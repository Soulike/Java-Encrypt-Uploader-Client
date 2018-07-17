package util;

import util.Objects.Message;
import util.Objects.UploadFileInfo;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.security.*;
import java.util.List;

import static util.AESKeyGenerator.getAESKey;
import static util.FileStructureReader.getAllFiles;

public class EncryptedFileUploader
{
    private final MyLogger logger;

    private final Key key;
    private final IvParameterSpec iv;

    private String address;
    private int port;

    private static final String ENCRYPT_MODE = "AES/CFB8/NoPadding";

    /**
     * 构造函数
     *
     * @param address 要连接的服务器地址
     * @param port    服务器监听端口
     */
    public EncryptedFileUploader(String address, int port)
    {
        this.address = address;
        this.port = port;
        this.key = getAESKey("JavaUploader");
        this.iv = new IvParameterSpec("1122334455667788".getBytes());
        this.logger = new MyLogger("上传模块");
    }

    /**
     * 上传指定文件/文件夹。如果是文件夹将会递归所有结构后上传。
     *
     * @param filePath 要被上传的文件/文件夹路径
     */
    public void upload(Path filePath) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        // 连接服务器
        Socket socket = new Socket(address, port);

        // 创建 Cipher
        Cipher outCipher = Cipher.getInstance(ENCRYPT_MODE);
        Cipher inCipher = Cipher.getInstance(ENCRYPT_MODE);
        outCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        inCipher.init(Cipher.DECRYPT_MODE, key, iv);

        // 记录时间戳，用于计算时间以及上传速度
        long startTime = 0;
        long endTime = 0;

        // 加密输出流
        final CipherOutputStream encryptedOut = new CipherOutputStream(socket.getOutputStream(), outCipher);
        // 解密输入流
        final CipherInputStream decryptedIn = new CipherInputStream(socket.getInputStream(), inCipher);

        // 对象输出流
        final ObjectOutputStream objOut = new ObjectOutputStream(encryptedOut);
        // 数据输出流
        final DataOutputStream dataOut = new DataOutputStream(encryptedOut);

        // 文件读取输入流
        DataInputStream fileIn = null;

        // 读取文件缓冲区
        final byte[] buffer = new byte[512];

        // 得到该文件夹下所有的文件
        final List<Path> fileList = getAllFiles(filePath);

        // 需要得到父目录来保证文件的相对路径正确
        final Path parentPath = filePath.getParent();

        // 指向当前处理文件的文件对象
        UploadFileInfo currentFileInfo = null;
        File currentFile = null;

        // 遍历文件列表，逐个上传
        for (Path path : fileList)
        {
            currentFile = path.toFile();

            if (currentFile.isFile())
            {
                startTime = System.currentTimeMillis();
                System.out.printf("文件 %s 开始上传\n", currentFile.getName());

                // 生成并传输文件对象
                currentFileInfo = new UploadFileInfo(currentFile.getName(), currentFile.length(), true, parentPath.toAbsolutePath().relativize(path).toFile());
                fileIn = new DataInputStream(new FileInputStream(currentFile));
                objOut.writeObject(currentFileInfo);
                objOut.flush();

                // 传输文件数据
                int readBytes = 0;
                long totalBytes = 0;
                int lastMultiple = 0;
                int progressPrinted = 0;

                while ((readBytes = fileIn.read(buffer)) != -1)
                {
                    dataOut.write(buffer, 0, readBytes);
                    dataOut.flush();
                    totalBytes += readBytes;
                    // 是2%的整数倍，输出一个进度标志
                    if ((int) ((double) totalBytes * 50 / currentFileInfo.getFileSize()) > lastMultiple)
                    {
                        lastMultiple = (int) ((double) totalBytes * 50 / currentFileInfo.getFileSize());
                        progressPrinted++;
                        System.out.print("=");
                    }
                }
                // 有可能出现文件过小，补齐进度条
                for (long i = progressPrinted; i < 50; i++)
                {
                    System.out.print("=");
                }
                System.out.println();

                endTime = System.currentTimeMillis();

                double fileSizeInMB = currentFile.length() / 1024f / 1024f;
                double uploadTimeInSecond = (endTime - startTime) / 1000f < 0.01 ? 0.01 : (endTime - startTime) / 1000f;
                System.out.printf("%s %f MB 上传成功，用时 %.2f 秒, 平均上传速度 %.2f KB/S\n", currentFile.getName(), fileSizeInMB, uploadTimeInSecond, fileSizeInMB * 1000 / uploadTimeInSecond);
            }
            else if (currentFile.isDirectory())
            {
                // 生成并传输文件对象
                currentFileInfo = new UploadFileInfo(currentFile.getName(), 0, false, parentPath.toAbsolutePath().relativize(path).toFile());
                objOut.writeObject(currentFileInfo);
                objOut.flush();
            }
        }
        // 传输完成，关闭输出流告知服务器文件已经传输完毕
        socket.shutdownOutput();

        // 接收 Message 对象获取结果
        ObjectInputStream objIn = new ObjectInputStream(decryptedIn);
        Message message = (Message) objIn.readObject();
        if (message.isSuccessful())
        {
            logger.logInfo(message.getMessage());
        }
        else
        {
            logger.logError(message.getMessage());
        }
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }
}
