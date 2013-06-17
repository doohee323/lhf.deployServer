package lhf.launcher.support

class FileBinaryCategoryTest {
    void testDownloadBinaryFile() {
        def file = new File("logo.gif")
        use (FileBinaryCategory) {
            file << "http://www.google.com/images/logo.gif".toURL()
        }
        assert file.length() > 0
        file.delete()
    }
}

class FileBinaryCategory {
    def static leftShift(File a_file, URL a_url) {
        def input
        def output
        try {
            input = a_url.openStream()
            output = new BufferedOutputStream(new FileOutputStream(a_file))

            output << input
        }
        finally {
            input?.close()
            output?.close()
        }
    }
}

//class FileBinaryCategory{
//    def static leftShift(File file, URL url){
//       url.withInputStream {is->
//            file.withOutputStream {os->
//                def bs = new BufferedOutputStream( os )
//                bs << is
//            }
//        }
//    }
//}