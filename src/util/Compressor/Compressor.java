package util.Compressor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩文件或文件夹到 Zip 文件。
 *
 * @author soulike
 */
public class Compressor
{
    /**
     * 通过命令行参数可将指定文件压缩为 Zip 文件，存放在当前目录。
     */
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("参数数量错误");
        }
        else
        {
            Path path = Paths.get(args[0]);
            try
            {
                compress(path, Paths.get("./", String.format("%s.zip", path.getFileName().toString())));
            }
            catch (IOException e)
            {
                System.out.println("压缩失败");
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩一个文件/文件夹成为一个 Zip 文件。
     *
     * @param srcPath 要被压缩的文件/文件夹 Path 对象。
     * @param dstPath 被压缩文件 Path 对象。
     */
    public static void compress(Path srcPath, Path dstPath) throws IOException
    {
        if (Files.notExists(srcPath))
        {
            throw new FileNotFoundException(String.format("%s 不存在", srcPath.toString()));
        }

        if (Files.exists(dstPath))
        {
            Files.delete(dstPath);
        }

        Files.createDirectories(dstPath.getParent());
        Files.createFile(dstPath);

        try (FileOutputStream out = new FileOutputStream(dstPath.toFile());
             CheckedOutputStream cos = new CheckedOutputStream(out, new CRC32());
             ZipOutputStream zipOut = new ZipOutputStream(cos, StandardCharsets.UTF_8))
        {
            Path basePath = Paths.get("");
            compress(srcPath.toFile(), zipOut, basePath);
        }
    }

    private static void compress(File file, ZipOutputStream zipOut, Path basePath) throws IOException
    {
        if (file.isDirectory())
        {
            compressDirectory(file, zipOut, basePath);
        }
        else
        {
            compressFile(file, zipOut, basePath);
        }
    }

    /**
     * 压缩一个文件夹。
     *
     * @param dir      文件夹 File 对象。
     * @param zipOut   指向 Zip 文件的输出流。
     * @param basePath dir 所在的目录。
     */
    private static void compressDirectory(File dir, ZipOutputStream zipOut, Path basePath) throws IOException
    {
        Object[] fileList = Files.list(dir.toPath()).toArray();
        File file;
        ZipEntry entry = new ZipEntry(dir.toString() + "/");
        zipOut.putNextEntry(entry);

        for (Object obj : fileList)
        {
            file = ((Path) obj).toFile();
            compress(file, zipOut, Paths.get(basePath.toString(), dir.getName()));
        }
    }

    /**
     * 压缩一个文件。
     *
     * @param file     文件 File 对象。
     * @param zipOut   指向 Zip 文件的输出流。
     * @param basePath file 所在的目录。
     */
    private static void compressFile(File file, ZipOutputStream zipOut, Path basePath) throws IOException
    {
        if (Files.notExists(file.toPath()))
        {
            return;
        }

        try (DataInputStream in = new DataInputStream(new FileInputStream(file)))
        {
            ZipEntry entry = new ZipEntry(basePath + "/" + file.getName());
            zipOut.putNextEntry(entry);
            int readBytes = 0;
            byte data[] = new byte[1024 * 1024];
            while ((readBytes = in.read(data)) != -1)
            {
                zipOut.write(data, 0, readBytes);
            }
        }
    }
}
