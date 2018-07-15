import util.EncryptedFileUploader;
import util.MyLogger;

import javax.crypto.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;

public class FileClient
{

    private static final MyLogger logger = new MyLogger("文件上传");

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
                EncryptedFileUploader uploader = new EncryptedFileUploader(args[0], Integer.parseInt(args[1]));
                uploader.upload(filePath);
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
}
