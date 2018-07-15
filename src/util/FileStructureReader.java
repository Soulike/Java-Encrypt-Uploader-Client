package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStructureReader
{
    /**
     * 得到指定文件夹下所有的文件以及文件夹的 Path 对象
     *
     * @param folderPath 想要得到所有结构的文件夹 Path 对象
     */
    public static List<Path> getAllFiles(Path folderPath) throws IOException
    {
        ArrayList<Path> fileList = new ArrayList<>();
        // 如果是目录，就递归
        if (folderPath.toFile().isDirectory())
        {
            getAllFilesRecursive(folderPath, fileList);
        }
        // 如果传入的是个文件，就直接返回它自己
        else
        {
            fileList.add(folderPath);
        }
        return fileList;
    }

    private static void getAllFilesRecursive(Path path, List<Path> fileList) throws IOException
    {
        fileList.add(path);
        if (path.toFile().isDirectory())
        {
            Object[] list = Files.list(path).toArray();
            for (Object fileObj : list)
            {
                getAllFilesRecursive((Path) fileObj, fileList);
            }
        }
    }
}
