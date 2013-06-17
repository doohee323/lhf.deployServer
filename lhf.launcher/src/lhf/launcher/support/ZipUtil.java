package lhf.launcher.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

    private static final int COMPRESSION_LEVEL = 8;

    private static final int BUFFER_SIZE = 1024 * 2;

    /**
     * log 처리를 위한 변수 선언
     */
    private static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 지정된 폴더를 Zip 파일로 압축한다.
     * @param sourcePath - 압축 대상 디렉토리
     * @param output - 저장 zip 파일 이름
     * @throws Exception
     */
    public static void zip(String sourcePath, String output){
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try{
            // 압축 대상(sourcePath)이 디렉토리나 파일이 아니면 리턴한다.
            File sourceFile = new File(sourcePath);
            if(!sourceFile.isFile() && !sourceFile.isDirectory()){
                System.out.println("압축 대상의 파일을 찾을 수가 없습니다.");
            }

            // output 의 확장자가 zip이 아니면 리턴한다.
            //            if(!(StringUtils.substringAfterLast(output, ".")).equalsIgnoreCase("zip")){
            //                System.out.println("압축 후 저장 파일명의 확장자를 확인하세요");
            //            }

            fos = new FileOutputStream(output); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8
            zipEntry(sourceFile, sourcePath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            if(bos != null){
                try{
                    bos.close();
                }catch(Exception e){
                }
            }
            if(fos != null){
                try{
                    fos.close();
                }catch(Exception e){
                }
            }
            if(zos != null){
                try{
                    zos.close();
                }catch(Exception e){
                }
            }
        }
    }

    /**
     * 압축
     * @param sourceFile
     * @param sourcePath
     * @param zos
     * @throws Exception
     */
    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception{
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if(sourceFile.isDirectory()){
            if(sourceFile.getName().equalsIgnoreCase(".metadata")){ // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
            for(int i = 0; i < fileArray.length; i++){
                zipEntry(fileArray[i], sourcePath, zos); // 재귀 호출
            }
        }else{ // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;
            try{
                String sFilePath = sourceFile.getPath();
                String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());

                bis = new BufferedInputStream(new FileInputStream(sourceFile));
                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;
                while((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1){
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            }finally{
                if(bis != null){
                    bis.close();
                }
            }
        }
    }

    /**
     * Zip 파일의 압축을 푼다.
     *
     * @param zipFile - 압축 풀 Zip 파일
     * @param targetDir - 압축 푼 파일이 들어간 디렉토리
     * @param fileNameToLowerCase - 파일명을 소문자로 바꿀지 여부
     * @throws Exception
     */
    public static void unzip(String zipFile, String targetDir) throws Exception{
        unzip(new File(zipFile), new File(targetDir), true);
    }

    public static void unzip(File zipFile, File targetDir, boolean fileNameToLowerCase) throws Exception{
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry zentry = null;

        try{
            fis = new FileInputStream(zipFile); // FileInputStream
            zis = new ZipInputStream(fis, Charset.forName("utf-8")); // ZipInputStream

            try{
                while((zentry = zis.getNextEntry()) != null){
                    String fileNameToUnzip = zentry.getName();
                    if(fileNameToLowerCase){ // fileName toLowerCase
                        fileNameToUnzip = fileNameToUnzip.toLowerCase();
                    }

                    File targetFile = new File(targetDir, fileNameToUnzip);

                    if(zentry.isDirectory()){// Directory 인 경우
                        new File(targetFile.getAbsolutePath()).mkdirs();
                    }else{ // File 인 경우
                           // parent Directory 생성
                        new File(targetFile.getParent()).mkdirs();
                        unzipEntry(zis, targetFile);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (Exception e) {
            System.out.println(e);
        }finally{
            if(zis != null){
                zis.close();
            }
            if(fis != null){
                fis.close();
            }
        }
    }

    /**
     * Zip 파일의 한 개 엔트리의 압축을 푼다.
     *
     * @param zis - Zip Input Stream
     * @param filePath - 압축 풀린 파일의 경로
     * @return
     * @throws Exception
     */
    protected static File unzipEntry(ZipInputStream zis, File targetFile) throws Exception{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(targetFile);

            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while((len = zis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            System.out.println(e);
        }finally{
            if(fos != null){
                fos.close();
            }
        }
        return targetFile;
    }

    public static void unTar(String inFilename, String outFilePath) throws Exception{
        try{
            TarInputStream tin = new TarInputStream(new GZIPInputStream(new FileInputStream(new File(inFilename))));
            TarEntry tarEntry = tin.getNextEntry();
            while(tarEntry != null){
                File destPath = new File(outFilePath + File.separatorChar + tarEntry.getName());
                if(tarEntry.isDirectory()){
                    destPath.mkdirs();
                }else{
                    String destDir = destPath.toString().substring(0,
                            destPath.toString().lastIndexOf(File.separatorChar));
                    if(!new File(destDir).exists()){
                        new File(destDir).mkdirs();
                    }
                    FileOutputStream fout = new FileOutputStream(destPath);
                    tin.copyEntryContents(fout);
                    fout.close();
                }
                tarEntry = tin.getNextEntry();
            }
            tin.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    /**
     * <pre>
     * unZipString
     * </pre>
     *
     * @param zipStr
     * @param unZipFile
     * @throws Exception
     */
    public static void unZipString(String zipStr, String unZipFile) throws Exception{
        try{
            byte o[] = zipStr.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(o);
            ZipInputStream in = new ZipInputStream(new ObjectInputStream(bi));
            while((in.getNextEntry()) != null){
                OutputStream out = new FileOutputStream(unZipFile);
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                }
                out.close();
            }
            in.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * <pre>
     * inputFile 객체를 압축하여, outFile 객체에 write 한다.
     * </pre>
     *
     * @param inputFile 압축대상파일
     * @param outFile 압축파일
     * @return boolean 성공 여부
     */
    public static boolean toZipFile(File inputFile, File outFile){
        try{
            byte b[] = new byte[512];

            FileOutputStream fos = new FileOutputStream(outFile);
            ZipOutputStream zout = new ZipOutputStream(fos);

            InputStream in = new FileInputStream(inputFile);
            ZipEntry e = new ZipEntry(inputFile.getName());
            zout.putNextEntry(e);
            int len = 0;
            while((len = in.read(b)) != -1){
                zout.write(b, 0, len);
            }
            zout.closeEntry();
            zout.close();
            fos.close();
        }catch(Exception e){
            logger.debug(e.toString());
            return false;
        }
        return true;
    }

    /**
     * <pre>
     * inputFile 객체를 압축하여, outFile 객체에 write 한다.
     * </pre>
     *
     * @param inputFile 압축대상파일
     * @param outFile 압축파일
     * @return boolean 성공 여부
     */
    public static void unZipFile(String readZip, String unZipFile) throws Exception{
        try{
            ZipInputStream in = new ZipInputStream(new FileInputStream(readZip));
            while((in.getNextEntry()) != null){
                OutputStream out = new FileOutputStream(unZipFile);
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                }
                out.close();
            }
            in.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * <pre>
     * inputFile 객체를 압축하여, outFile 객체에 write 한다.
     * </pre>
     *
     * @param inputFile 압축대상파일
     * @param outFile 압축파일
     * @return boolean 성공 여부
     */
    public static boolean toGZipFile(String inputFile, String outputFile){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPOutputStream gzos = null;
        try{
            File inFile = new File(inputFile);
            fis = new FileInputStream(inFile);

            // 압축해서 임시파일로 저장.
            File gzipFile = new File(outputFile);
            fos = new FileOutputStream(gzipFile);

            // GZip 압축 스트림 생성
            gzos = new GZIPOutputStream(fos);
            logger.debug("GZip으로 압축할 타켓 파일 이름 : " + inFile.getName());

            // 버퍼 셋팅
            byte[] buffer = new byte[1024 * 8];
            int nRead;

            // 원본파일 -> 압축임시파일로 만듬.
            while((nRead = fis.read(buffer)) != -1){
                gzos.write(buffer, 0, nRead);
            }

            gzos.close();
            logger.debug("압축한 임시파일 이름 : " + gzipFile.getName() + " make Sucessed");
            logger.debug(gzipFile.getName() + " Size = " + gzipFile.length() + " byte");
        }catch(Exception e){
            logger.debug(e.toString());
        }finally{
            try{
                fis.close();
            }catch(Exception e){
            }
            try{
                fos.close();
            }catch(Exception e){
            }
            try{
                gzos.close();
            }catch(Exception e){
            }
        }
        return true;
    }

    public static void unGz(String inFilename, String outFilePath) throws Exception{
        try{
            GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inFilename));
            OutputStream out = new FileOutputStream(outFilePath);
            byte[] buf = new byte[1024];
            int len;
            while((len = gzipInputStream.read(buf)) > 0)
                out.write(buf, 0, len);
            gzipInputStream.close();
            out.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     * <pre>
     * inputFile 객체를 압축하여, String 객체에 write 한다.
     * </pre>
     *
     * @param fileName 압축대상파일
     * @throws Exception
     * @return boolean 성공 여부
     */
    public static String toZipString(String fileName) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            byte b[] = new byte[512];
            ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(baos));
            InputStream in = new FileInputStream(new File(fileName));
            ZipEntry e = new ZipEntry(fileName);
            zout.putNextEntry(e);
            int len = 0;
            while((len = in.read(b)) != -1){
                zout.write(b, 0, len);
            }
            zout.closeEntry();
            zout.close();
            baos.close();
        }catch(Exception e){
            logger.debug(e.toString());
            return "";
        }
        return baos.toString(); //SecurityUtil.base64Encode(baos.toString());
    }

    public static void main(String[] args) throws Exception{
//        zip("C:/share/build/lhf.lorisg/tmp", "C:/share/work/lhf.lorisg/lhf.lorisg.zip");
        unzip("C:/share/work/lhf.lorisg/lhf.lorisg.zip", "C:/share/build/lhf.lorisg/tmp");
    }
}
