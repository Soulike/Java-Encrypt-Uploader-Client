import util.Logger.MyLogger;
import util.Objects.Message;
import util.Objects.UploadFileInfo;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static util.Compressor.Compressor.*;

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

public class FileClient
{
    private static String ENCRYPT_MODE = "AES/CFB8/NoPadding";
    private static MyLogger logger = new MyLogger("文件上传");

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("参数数量错误，请输入正确参数。例如: java FileClient 1.2.3.4 6666 /home/java/");
        }
        else
        {
            Path filePath = Paths.get(args[2]);
            // 如果是目录，就需要打包
            try
            {
                // TODO: 文件夹打包方法有问题
                if (filePath.toFile().isDirectory())
                {
                    try
                    {
                        Path zippedFilePath = Files.createTempFile(filePath.toFile().getName(), ".zip");
                        compress(filePath, zippedFilePath);
                        uploadFile(args[0], Integer.parseInt(args[1]), zippedFilePath, true);
                        Files.delete(zippedFilePath);
                    }
                    catch (IOException e)
                    {
                        System.out.println("创建临时文件失败");
                        e.printStackTrace();
                    }

                }
                // 如果不是目录，直接上传
                else if (filePath.toFile().isFile())
                {
                    logger.logInfo("开始上传");
                    uploadFile(args[0], Integer.parseInt(args[1]), filePath, false);
                }
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


    private static void uploadFile(String address, int port, Path filePath, boolean isZipped) throws IOException, ClassNotFoundException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        Cipher cipher;
        Key key = getKey("JavaUploader");
        cipher = Cipher.getInstance(ENCRYPT_MODE);
        IvParameterSpec iv = new IvParameterSpec("1122334455667788".getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        Socket socket = new Socket(address, port);
        InputStream rawIn = socket.getInputStream();
        OutputStream rawOut = socket.getOutputStream();
        CipherOutputStream out = new CipherOutputStream(rawOut, cipher);

        ObjectOutputStream objOut = new ObjectOutputStream(out);

        File file = filePath.toFile();
        UploadFileInfo fileInfo = new UploadFileInfo(file.getName(), file.length(), isZipped);
        objOut.writeObject(fileInfo);
        objOut.flush();

        DataOutputStream dataOut = new DataOutputStream(out);
        DataInputStream fileDataIn = new DataInputStream(new FileInputStream(filePath.toFile()));
        byte[] buffer = new byte[256];
        int readBytes = 0;
        while ((readBytes = fileDataIn.read(buffer)) != -1)
        {
            dataOut.write(buffer, 0, readBytes);
            dataOut.flush();
        }
        fileDataIn.close();
        socket.shutdownOutput();

        ObjectInputStream dataIn = new ObjectInputStream(rawIn);
        Message message = (Message) dataIn.readObject();
        if (message.isSuccessful())
        {
            logger.logInfo(message.getMessage());
        }
        else
        {
            logger.logError(message.getMessage());
        }
        socket.close();
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
}
