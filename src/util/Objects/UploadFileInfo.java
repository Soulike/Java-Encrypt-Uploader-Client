package util.Objects;

import java.io.File;
import java.io.Serializable;

/**
 * 上传文件文件信息对象，放置在上传流的首部。
 *
 * @author soulike
 */
public class UploadFileInfo implements Serializable
{
    /**
     * 文件名（包含扩展名）。
     */
    private final String fileName;
    /**
     * 文件大小（字节）。
     */
    private final long fileSize;
    /**
     * 是文件还是文件夹。
     */
    private final boolean isFile;

    /**
     * 存储文件路径的 File 对象，相对于上传的文件夹。
     * 例如要上传的文件夹为 corejava，该路径可能为 corejava/folder1/1.txt。
     */
    private final File file;

    /**
     * 构造函数，指定该文件属性
     *
     * @param fileName 文件名（包含扩展名）
     * @param fileSize 文件大小（字节）
     * @param isFile   是文件还是文件夹
     * @param file     存储文件路径的 File 对象
     */
    public UploadFileInfo(String fileName, long fileSize, boolean isFile, File file)
    {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isFile = isFile;
        this.file = file;
    }

    /**
     * 获取文件名（包含扩展名）
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * 获取文件大小（字节）
     */
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * 该文件是文件还是文件夹
     */
    public boolean isFile()
    {
        return isFile;
    }

    /**
     * 获得存储文件路径的 File 对象，相对于上传的文件夹。
     */
    public File getFile()
    {
        return file;
    }
}
