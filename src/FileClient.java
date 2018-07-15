import util.MyLogger;
import util.Objects.*;

import static util.FileStructureReader.*;
import static util.AESKeyGenerator.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;


import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.security.*;
import java.util.List;

public class FileClient
{
    private static final String ENCRYPT_MODE = "AES/CFB8/NoPadding";
    private static final MyLogger logger = new MyLogger("文件上传");
    private static Cipher outCipher;
    private static Cipher inCipher;

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("参数数量错误，请输入正确参数。例如: java FileClient 1.2.3.4 6666 /home/java/");
        }
        else
        {
            final Path filePath = Paths.get(args[2]);
            try
            {
                final Socket socket = connectServer(args[0], Integer.parseInt(args[1]));
                uploadFile(filePath, socket);
                socket.close();
            }
            catch (IOException e)
            {
                logger.logError("与服务器的连接失败");
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                logger.logError("服务器返回异常");
                e.printStackTrace();
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e)
            {
                logger.logError("加密出现问题");
                e.printStackTrace();
            }
        }
    }


    /**
     * 上传指定文件/文件夹。如果是文件夹将会递归所有结构后上传。
     *
     * @param filePath 要被上传的文件/文件夹路径
     * @param socket   与服务器建立的 Socket 对象
     */
    private static void uploadFile(Path filePath, Socket socket) throws IOException, ClassNotFoundException
    {
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

                // 生成并传输文件对象
                currentFileInfo = new UploadFileInfo(currentFile.getName(), currentFile.length(), true, parentPath.toAbsolutePath().relativize(path).toString());
                fileIn = new DataInputStream(new FileInputStream(currentFile));
                objOut.writeObject(currentFileInfo);
                objOut.flush();

                // 传输文件数据
                int readBytes = 0;
                while ((readBytes = fileIn.read(buffer)) != -1)
                {
                    dataOut.write(buffer, 0, readBytes);
                    dataOut.flush();
                }

                endTime = System.currentTimeMillis();

                double fileSizeInMB = currentFile.length() / 1024f / 1024f;
                double uploadTimeInSecond = (endTime - startTime) / 1000f == 0 ? 0.01 : (endTime - startTime) / 1000f;
                System.out.printf("%s %f MB 上传成功，用时 %.2f 秒, 平均上传速度 %.2f KB/S\n", currentFile.getName(), fileSizeInMB, uploadTimeInSecond, fileSizeInMB * 1000 / uploadTimeInSecond);
            }
            else if (currentFile.isDirectory())
            {
                // 生成并传输文件对象
                currentFileInfo = new UploadFileInfo(currentFile.getName(), 0, false, parentPath.toAbsolutePath().relativize(path).toString());
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

    private static Socket connectServer(String address, int port) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidAlgorithmParameterException
    {

        Key key = getAESKey("JavaUploader");
        outCipher = Cipher.getInstance(ENCRYPT_MODE);
        inCipher = Cipher.getInstance(ENCRYPT_MODE);
        IvParameterSpec iv = new IvParameterSpec("1122334455667788".getBytes());
        outCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        inCipher.init(Cipher.DECRYPT_MODE, key, iv);
        return new Socket(address, port);
    }
}
