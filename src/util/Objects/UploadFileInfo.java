package util.Objects;

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
     * 文件路径，相对于上传的文件夹。
     * 例如要上传的文件夹为 corejava，该路径可能为 corejava/folder1/1.txt。
     */
    private final String filePath;

    /**
     * 构造函数，指定该文件属性
     *
     * @param fileName 文件名（包含扩展名）
     * @param fileSize 文件大小（字节）
     * @param isFile   是文件还是文件夹
     */
    public UploadFileInfo(String fileName, long fileSize, boolean isFile, String filePath)
    {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isFile = isFile;
        this.filePath = filePath;
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
     * 获得文件路径，相对于上传的文件夹。
     */
    public String getFilePath()
    {
        return filePath;
    }
}
