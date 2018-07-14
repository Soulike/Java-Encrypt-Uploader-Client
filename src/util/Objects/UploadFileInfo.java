package util.Objects;

import java.io.Serializable;

/**
 * 上传文件文件信息对象，放置在上传流的首部。
 *
 * @author soulike
 */
public class UploadFileInfo implements Serializable
{
    private final String fileName;
    private final long fileSize;
    private final boolean needUnzip;

    /**
     * @param fileName  要上传文件的名字。
     * @param fileSize  要上传文件的大小。
     * @param needUnzip 文件上传后是否需要解压缩。
     */
    public UploadFileInfo(String fileName, long fileSize, boolean needUnzip)
    {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.needUnzip = needUnzip;
    }

    public String getFileName()
    {
        return fileName;
    }

    public long getFileSize()
    {
        return fileSize;
    }

    public boolean isZipped()
    {
        return needUnzip;
    }
}
