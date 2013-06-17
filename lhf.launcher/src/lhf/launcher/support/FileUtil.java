package lhf.launcher.support;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * ---------------------------------------------------------------
 * 업무구분 :
 * 프로그램 :
 * 설    명 : Util성 기능들을 제공하는 클래스 이다.
 * 작 성 자 : LHF
 * 작성일자 :
 * 수정이력
 * ---------------------------------------------------------------
 * 수정일          이  름    사유      
 * ---------------------------------------------------------------
 *
 * ---------------------------------------------------------------
 * </pre>
 *
 * @version 1.0
 */
public class FileUtil {

    /**
     * log 처리를 위한 변수 선언
     */
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * <pre>
     * 도스 파일시스템의 seperator(\)를 Java Style (/)로 변경하기 위해 사용되는 정규식 패턴
     * </pre>
     */
    public static final Pattern dosSeperator = Pattern.compile("\\\\");

    /**
     * <pre>
     * 파일을 복사하는 기능을 제공하여 준다.
     * </pre>
     *
     * @param in 원본 File 객체
     * @param out Target File 객체
     * @return boolean 파일복사의 성공여부
     */
    public static boolean copyFile(File in, File out){
        try{
            File directory = new File(out.getParent());
            if(directory.isDirectory() == false)
                directory.mkdirs();

            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out).getChannel();

            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

            sourceChannel.close();
            destinationChannel.close();
            return true;
        }catch(IOException ioe){
            logger.debug("[M] copyFile() Error Message : " + ioe.toString());
            return false;
        }
    }

    /**
     * <pre>
     * newFileName
     * </pre>
     *
     * @param fileName
     * @param gubun
     * @return fileName
     */
    public static String newFileName(String fileName, String gubun){
        String path = null;
        String fileName2 = null;
        String extend = null;

        try{
            path = fileName.substring(0, fileName.lastIndexOf("/") + 1); // 경로만
            fileName2 = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")); // 확장자 뺀 파일명
            extend = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()); // 확장자만
            File input = new File(fileName);

            if(input.exists()){ // 파일이 존재할 경우
                fileName = newFileName(path + fileName2 + gubun + "." + extend, gubun);
            }else{

            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return fileName;
    }

    /**
     * <pre>
     * 파일을 복사하는 기능을 제공하여 준다.
     * </pre>
     *
     * @param in 원본 File 객체
     * @param out Target File 객체
     * @return boolean 파일복사의 성공여부
     */
    public static boolean copyFile(String in, String out){
        return copyFile(new File(in), new File(out));
    }

    /**
     * <pre>
     * 파일을 복사하는 기능을 제공하여 준다.
     * </pre>
     *
     * @param in 원본 File 객체
     * @param out Target File 객체
     * @param gubun
     * @return boolean 파일복사의 성공여부
     */
    public static boolean copyFile(String in, String out, String gubun){
        return copyFile(new File(in), new File(newFileName(out, gubun)));
    }

    /**
     * <pre>
     * 파일을 이동하는 기능을 제공하여 준다.
     * </pre>
     *
     * @param in 원본 File 객체
     * @param out Target File 객체
     * @param gubun
     * @return boolean 파일이동의 성공여부
     */
    public static boolean moveFile(String in, String out){
        if(!new File(out).exists()){
            new File(out).mkdirs();
        }
        copyFile(in, out);
        return new File(in).delete();
    }

    /**
     * <pre>
     * 주어진 파일의 fullpath중 filname part를 제외한 path 부분만 분리하여 리턴한다.<BR>
     * (new File(fullpath)).getParent()와 동일하나 File 객체를 사용하지 않고 문자열 패턴만으로 분석한다.<BR>
     * 만약 fullpath에 / 혹은 \가 존재하지 않는 경우라면 "./" 을 리턴할 것이다.<BR>
     * 만약 \이 fullpath에 존재한다면 모두 /로 변경할 것이다.
     * </pre>
     *
     * @param fullpath Path와 filename으로 이루어진 파일의 fullpath
     * @return String fullpath중 path
     */
    public static String getFilePathChop(String fullpath){
        if(null == fullpath)
            return null;
        fullpath = dosSeperator.matcher(fullpath).replaceAll("/");
        int pos = fullpath.lastIndexOf("/");
        if(pos > -1)
            return fullpath.substring(0, pos + 1);
        else
            return "./";
    }

    /**
     * <pre>
     * 주어진 파일의 fullpath중 path부분을 제외한 filname part만 분리하여 리턴한다.<BR>
     * (new File(fullpath)).getName()과 동일하나 File 객체를 사용하지 않고 문자열 패턴만으로 분석한다.<BR>
     * 만약 fullpath에 / 혹은 \가 존재하지 않는 경우라면 "./" 을 리턴할 것이다.
     * </pre>
     *
     * @param fullpath Path와 filename으로 이루어진 파일의 fullpath
     * @return String fullpath중 filename part
     */
    public static String getFileNameChop(String fullpath){
        if(fullpath == null)
            return null;
        fullpath = dosSeperator.matcher(fullpath).replaceAll("/");
        int pos = fullpath.lastIndexOf("/");
        if(pos > -1)
            return fullpath.substring(pos + 1);
        return fullpath;
    }

    /**
     * <pre>
     * 파일명을 수정하는 기능을 제공해준다.
     * </pre>
     *
     * @param in 원본 File 명
     * @param out Target File 명
     * @return boolean 파일명 변경의 성공여부
     */
    public synchronized static String renameFileInSameDir(String in, String out){

        File inFile = new File(in);

        String filename = inFile.getName();
        int lastDot = filename.lastIndexOf('.');
        String extension = (lastDot == -1) ? "" : filename.substring(lastDot);

        File outFile = new File(getCompleteLeadingSeperator(inFile.getParent()) + out + extension);

        return renameFile(inFile, outFile);
    }

    /**
     * <pre>
     * 파일명을 수정하는 기능을 제공해준다.
     * </pre>
     *
     * @param in 원본 File 명
     * @param out Target File 명
     * @return boolean 파일명 변경의 성공여부
     */
    public static String renameFile(String in, String out){

        return renameFile(new File(in), new File(out));
    }

    /**
     * <pre>
     * 파일명을 수정하는 기능을 제공해준다.
     * </pre>
     *
     * @param in 원본 File 객체
     * @param out Target File 객체
     * @return boolean 파일명 변경의 성공여부
     */
    public static String renameFile(File in, File out){

        if(out.canRead())
            out.delete();

        if(in.renameTo(out))
            return out.getAbsolutePath();
        else
            return null;
    }

    /**
     * <pre>
     * filePath에 해당하는 파일을 삭제한다.
     * </pre>
     *
     * @param filePath 파일 패스
     * @return boolean 삭제 성공 여부
     */
    public synchronized static boolean removeFile(String filePath){
        File file = new File(filePath);
        return file.delete();
    }

    /**
     * <pre>
     * File 객체를 Byte[] 형태로 리턴해준다.
     * </pre>
     *
     * @param file 대상파일
     * @throws IOException
     * @return 바이트 array화된 파일
     */
    public static byte[] getBytesFromFile(File file) throws IOException{
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if(length > Integer.MAX_VALUE){
            // File is too large
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while(offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0){
            offset += numRead;
        }
        if(offset < bytes.length){
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    /**
     * <pre>
     * OutputStream에 바이트 정보를 write 한다.
     * </pre>
     *
     * @param out
     * @param fileByte
     */
    public static void write(OutputStream out, byte[] fileByte) throws IOException{

        if(fileByte.length > 0){
            byte[] by = new byte[fileByte.length];
            ByteArrayInputStream in = new ByteArrayInputStream(fileByte);
            BufferedOutputStream buffOuts = new BufferedOutputStream(out);

            int read = 0;

            while((read = in.read(by)) != -1){
                buffOuts.write(by, 0, read);
            }

            buffOuts.close();
            in.close();
        }
    }

    /**
     * <pre>
     * OutputStream에 파일 정보를 write 한다.
     * </pre>
     *
     * @param out
     * @param file
     */
    public static void write(OutputStream out, File file) throws IOException{

        byte[] fileByte = getBytesFromFile(file);

        write(out, fileByte);
    }

    /**
     * <pre>
     * OutputStream에 파일 정보를 delete 한다.
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static boolean delete(String filePath){
        File file = new File(filePath);

        if(file.isFile())
            return file.delete();
        else
            return true;
    }

    /**
     * <pre>
     * File 객체를 StringBuffer 형태로 리턴해준다.
     * </pre>
     *
     * @param file 대상파일
     * @return String
     */
    public static StringBuffer getFromFile(String fileName) throws IOException{
        return getFromFile(fileName, null);
    }

    /**
     * <pre>
     * File 객체를 StringBuffer 형태로 리턴해준다.
     * </pre>
     *
     * @param file 대상파일
     * @param strChar
     * @return String
     */
    public static StringBuffer getFromFile(String fileName, String strChar) throws IOException{
        if(strChar == null){
            Scanner scanner = new Scanner(new File(fileName)).useDelimiter("\\Z");
            String contents = scanner.next();
            scanner.close();
            return new StringBuffer(contents);
        }

        if(strChar.equals(""))
            strChar = null;

        StringBuffer sb = new StringBuffer(1000);
        InputStreamReader is = null;
        BufferedReader in = null;
        String lineSep = System.getProperty("line.separator");

        try{
            File f = new File(fileName);
            if(f.exists()){
                if(strChar != null)
                    is = new InputStreamReader(new FileInputStream(f), strChar);
                else
                    is = new InputStreamReader(new FileInputStream(f));
                in = new BufferedReader(is);
                String str = "";

                int readed = 0;
                while((str = in.readLine()) != null){
                    if(strChar != null)
                        readed += (str.getBytes(strChar).length);
                    else
                        readed += (str.getBytes().length);
                    sb.append(str + lineSep);
                }
            }
        }catch(Exception e){
            logger.debug(e.toString());
        }finally{
            if(is != null)
                is.close();
            if(in != null)
                in.close();
        }
        return sb;
    }

    /**
     * <pre>
     * OutputStream에 바이트 정보를 write 한다.
     * </pre>
     *
     * @param aFileNm
     * @param sb
     * @param aCharSet
     * @throws IOException
     */
    public static void write(String aFileNm, StringBuffer sb, String aCharSet) throws IOException{
        write(aFileNm, sb, aCharSet, false);
    }

    /**
     * <pre>
     * OutputStream에 바이트 정보를 write 한다.
     * </pre>
     *
     * @param aFileNm
     * @param sb
     * @param aCharSet
     * @param bChk
     * @throws IOException
     */
    public static void write(String aFileNm, StringBuffer sb, String aCharSet, boolean bChk) throws IOException{
        OutputStreamWriter os = null;
        FileOutputStream fos = null;
        PrintWriter outp = null;
        try{
            String path = "";
            aFileNm = aFileNm.replace("\\", "/");
            if(aFileNm.indexOf("/") > -1)
                path = aFileNm.substring(0, aFileNm.lastIndexOf("/"));
            if(!new File(path).exists())
                new File(path).mkdirs();
            // bChk : true 이어쓰기
            fos = new FileOutputStream(aFileNm, bChk);
            if(aCharSet == null){
                os = new OutputStreamWriter(fos);
            }else{
                os = new OutputStreamWriter(fos, aCharSet);
            }
            outp = new PrintWriter(os, true);
            outp.println(sb);
        }catch(Exception e){
            logger.debug(e.toString());
        }finally{
            if(os != null)
                os.close();
            if(fos != null)
                fos.close();
            if(outp != null)
                outp.close();
        }
    }

    /**
     * <pre>
     * 파일이 위치한 디렉토리 리턴
     * </pre>
     *
     * @param strPath
     * @return strPath
     */
    public static String getFileDir(String strPath){
        try{
            strPath = strPath.replace("\\", "/");
            String strArry[] = strPath.split("/");
            String strFileNm = strArry[strArry.length - 1];
            strPath = strPath.substring(0, strPath.indexOf(strFileNm) - 1);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return strPath;
    }

    /**
     * <pre>
     * getCompleteLeadingSeperator
     * </pre>
     *
     * @param filePath
     * @return filePath
     */
    public static String getCompleteLeadingSeperator(String filePath){
        if(null == filePath)
            return null;
        filePath = dosSeperator.matcher(filePath).replaceAll("/");
        if(!filePath.endsWith(File.separator) && !filePath.endsWith("/"))
            filePath += "/";
        return filePath = filePath.replaceAll("//", "/");
    }

    /**
     * <pre>
     * makeDir
     * </pre>
     *
     * @param directory
     * @throws Exception
     */
    public static void makeDir(String directory) throws Exception{
        if(directory == null || directory.length() == 0)
            throw new Exception("directory can not be founded");
        directory = getCompleteLeadingSeperator(directory);

        String strDir[] = directory.split("/");
        String subDir = "";
        for(int i = 0; i < strDir.length; i++){
            subDir += strDir[i] + "/";
            File file = new File(subDir);
            if(!file.exists()){
                file.mkdir();
            }
        }
    }

    /**
     * <pre>
     * getMimeType
     * </pre>
     *
     * @param s
     * @return
     */
    public static String getMimeType(String s){
        int i = s.lastIndexOf(".");
        if(i > 0 || i < s.length() - 1)
            return getMime(s.substring(i + 1, s.length()));
        else
            return "application/octet-stream";
    }

    /**
     * <pre>
     * getMime
     * </pre>
     *
     * @param s
     * @return
     */
    public static String getMime(String s){
        String s1 = s.toUpperCase();
        if(s1.equals("GIF"))
            return "image/gif";
        if(s1.equals("JPG") || s1.equals("JPEG") || s1.equals("JPE"))
            return "image/jpeg";
        if(s1.startsWith("TIF"))
            return "image/tiff";
        if(s1.startsWith("PNG"))
            return "image/png";
        if(s1.equals("IEF"))
            return "image/ief";
        if(s1.equals("BMP"))
            return "image/bmp";
        if(s1.equals("RAS"))
            return "image/x-cmu-raster";
        if(s1.equals("PNM"))
            return "image/x-portable-anymap";
        if(s1.equals("PBM"))
            return "image/x-portable-bitmap";
        if(s1.equals("PGM"))
            return "image/x-portable-graymap";
        if(s1.equals("PPM"))
            return "image/x-portable-pixmap";
        if(s1.equals("RGB"))
            return "image/x-rgb";
        if(s1.equals("XBM"))
            return "image/x-xbitmap";
        if(s1.equals("XPM"))
            return "image/x-xpixmap";
        if(s1.equals("TXT"))
            return "text/plain";
        if(s1.equals("XWD"))
            return "image/x-xwindowdump";
        else
            return "application/octet-stream";
    }

    /**
     * <pre>
     * getLastModified
     * </pre>
     *
     * @param arg
     * @return str
     */
    public static String getLastModified(long arg){
        String str = "";
        try{
            Date date = new Date();
            date.setTime(arg);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy년MM월dd일 HH:mm:ss:SSS");
            str = formatter.format(date);
        }catch(Exception e){
            logger.debug("[M] getLastModified() Error Message : " + e.toString());
        }
        return str;
    }

    /**
     * <pre>
     * - 정의 : LHF 기본 파일 명명 패턴에서 파일명 추출
     * </pre>
     *
     * @param serverFileName
     * @return serverFileName
     */
    public static String getFileNameByPattern(String serverFileName){
        return serverFileName;
    }

    /**
     * <pre>
     * - 정의 : Multi-app 환경에서의 파일 저장 디렉토리
     * </pre>
     *
     * @param config
     * @param sysCode
     * @param dateFolder
     * @return String
     */
    public static String getTargetDirectory(String config, String sysCode, String dateFolder){

        String targetDirectory = null;
        if(dateFolder == null || !dateFolder.equals("false")){
            targetDirectory += "/{date:yyyyMM}";
        }

        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(targetDirectory);
        String part;
        boolean fileExist = false;
        while(matcher.find()){
            part = matcher.group(1);
            if("date:".equals(part.substring(0, 5)))
                part = (new SimpleDateFormat(part.substring(5), java.util.Locale.KOREA)).format(new java.util.Date());
            int lastDot = targetDirectory.lastIndexOf("{");
            if(!targetDirectory.substring(lastDot - 1, lastDot).equals("/"))
                part = "/" + part;
            fileExist = true;
            matcher.appendReplacement(sb, part);
        }
        matcher.appendTail(sb);
        if(fileExist){
            File newFile = new File(sb.toString());
            if(!newFile.exists()){
                newFile.mkdir();
            }
        }
        logger.debug("[targetDirectory] " + sb.toString());
        return sb.toString();
    }

    /**
     * <pre>
     * 확장자를 제외한 파일명을 return 한다.
     * </pre>
     *
     * @param filename
     * @return name
     */
    public static String getWithoutExtension(String filename){
        int index = filename.lastIndexOf(".");
        String name = index == -1 ? filename : filename.substring(0, index);
        return name;
    }

    /**
     * <pre>
     * 파일의 확장자를 return 한다.
     * </pre>
     *
     * @param filename
     * @return ext
     */
    public static String getExtension(String filename){
        int index = filename.lastIndexOf(".");
        String ext = index == -1 ? "" : filename.substring(index + 1);
        return ext;
    }

    /**
     * <pre>
     * 파일을 무조건 덮어쓰게 하지 않기 위해, BackUp 파일을 생성한다.
     * </pre>
     *
     * @param wishFile
     * @param pattern
     * @return resultFile
     */
    public static File getUnOverwriteFile(File wishFile, String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, java.util.Locale.KOREA);
        String timestamp = dateFormat.format(new java.util.Date());
        String newFile = wishFile.getAbsolutePath();
        File resultFile = new File(getWithoutExtension(newFile) + "[" + timestamp + "]." + getExtension(newFile));

        while(resultFile.exists()){
            newFile = resultFile.getAbsolutePath();
            resultFile = new File(getWithoutExtension(newFile) + "[" + timestamp + "]." + getExtension(newFile));
        }
        return resultFile;
    }

    public static void main(String[] args) throws Exception{}
}
