import util.Logger.MyLogger;
import util.Objects.Message;
import util.Objects.UploadFileInfo;

import static util.FileTool.FileStructureReader.*;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FileClient
{
    private static String ENCRYPT_MODE = "AES/CFB8/NoPadding";
    private static MyLogger logger = new MyLogger("文件上传");
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


        CipherOutputStream encryptedOut = new CipherOutputStream(socket.getOutputStream(), outCipher);
        CipherInputStream decryptedIn = new CipherInputStream(socket.getInputStream(), inCipher);

        ObjectOutputStream objOut = new ObjectOutputStream(encryptedOut);

        DataOutputStream dataOut = new DataOutputStream(encryptedOut);
        DataInputStream fileIn = null;


        byte[] buffer = new byte[512];

        List<Path> fileList = getAllFiles(filePath);

        // 需要得到父目录来保证文件的相对路径正确
        Path parentPath = filePath.getParent();

        UploadFileInfo currentFileInfo = null;
        File currentFile = null;
        for (Path path : fileList)
        {
            currentFile = path.toFile();
            // 如果是一个文件，就上传
            if (currentFile.isFile())
            {
                startTime = System.currentTimeMillis();

                currentFileInfo = new UploadFileInfo(currentFile.getName(), currentFile.length(), true, parentPath.toAbsolutePath().relativize(path).toString());
                fileIn = new DataInputStream(new FileInputStream(currentFile));
                objOut.writeObject(currentFileInfo);
                objOut.flush();

                int readBytes = 0;
                while ((readBytes = fileIn.read(buffer)) != -1)
                {
                    dataOut.write(buffer, 0, readBytes);
                    dataOut.flush();
                }

                endTime = System.currentTimeMillis();

                double fileSizeInMB = currentFile.length() / 1024f / 1024f;
                double uploadTimeInSecond = (endTime - startTime) / 1000f == 0 ? 1 : (endTime - startTime) / 1000f;
                logger.logInfo(String.format("%s %f MB 上传成功，用时 %.2f 秒, 平均上传速度 %.2f KB/S", currentFile.getName(), fileSizeInMB, uploadTimeInSecond, fileSizeInMB * 1000 / uploadTimeInSecond));
            }
            else if (currentFile.isDirectory())
            {
                currentFileInfo = new UploadFileInfo(currentFile.getName(), 0, false, parentPath.toAbsolutePath().relativize(path).toString());
                objOut.writeObject(currentFileInfo);
                objOut.flush();
            }
        }
        socket.shutdownOutput();


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

    private static Key getKey(String password)
    {
        byte[] ketData = get16BytesKetData(password);
        return new SecretKeySpec(ketData, "AES");
    }

    private static byte[] get16BytesKetData(String password)
    {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] BytesKeyData = new byte[16];
        int copyLength = passwordBytes.length >= 16 ? 16 : passwordBytes.length;
        for (int i = 0; i < copyLength; i++)
        {
            BytesKeyData[i] = passwordBytes[i];
        }

        for (int i = copyLength; i < 16; i++)
        {
            BytesKeyData[i] = 0;
        }

        return BytesKeyData;
    }

    private static Socket connectServer(String address, int port) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidAlgorithmParameterException
    {

        Key key = getKey("JavaUploader");
        outCipher = Cipher.getInstance(ENCRYPT_MODE);
        inCipher = Cipher.getInstance(ENCRYPT_MODE);
        IvParameterSpec iv = new IvParameterSpec("1122334455667788".getBytes());
        outCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        inCipher.init(Cipher.DECRYPT_MODE, key, iv);
        return new Socket(address, port);
    }
}
