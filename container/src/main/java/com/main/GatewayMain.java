package com.main;//package com.main;
//
//
//import chat.main.ServerStart;
//import org.apache.commons.cli.*;
//
//import java.util.Arrays;
//
//public class GatewayMain {
//
//    /**
//     * 日志记录器
//     */
//
//    private static final int defaultPort = 8090;
//    private static final String jetty_home = ".";
//    private static final int MAX_THREADS = 1024;
//    private static int serverPort;
//    private static int maxThreadPoolSize;
//    private static int maxInnerThreadPoolSize;
//    private static String customWarPath;
//
////	private static Map<String, String> asyncMap;
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        CommandLineParser parser = new PosixParser();
//        Options opt = new Options();
//        opt.addOption("h", "help", false, "help")
//                .addOption("p",true, "port")
////			.addOption("a",true, "async servlet map")
//                .addOption("i",true, "Inner Max thread pool size")
//                .addOption("t",true, "Max thread pool size")
//                .addOption("w",true, "warPath--webContent path");
//
//        System.setProperty("jetty.home", jetty_home);
//        try {
//            CommandLine line = parser.parse(opt, args);
//            System.out.println("commandLine " + Arrays.toString(args));
//            if (line.hasOption('h') || line.hasOption("help")) {
//                HelpFormatter hf = new HelpFormatter();
//                hf.printHelp("BIStartServer[options:]", opt, false);
//                return;
//            }
//            int port = defaultPort;
//            if(line.hasOption('p')){
//                System.setProperty("jetty.port", line.getOptionValue('p'));
//                port = Integer.parseInt(line.getOptionValue('p'));
//            }else{
//                System.setProperty("jetty.port", defaultPort+"");
//            }
//            serverPort = port;
//
//            if(line.hasOption('t')){
//                maxThreadPoolSize = Integer.parseInt(line.getOptionValue('t'));
//            } else {
//                maxThreadPoolSize = MAX_THREADS;
//            }
//            if(line.hasOption('i')){
//                maxInnerThreadPoolSize = Integer.parseInt(line.getOptionValue('i'));
//            } else {
//                maxInnerThreadPoolSize = 0;
//            }
//            if(line.hasOption('w')){
//                customWarPath = line.getOptionValue('w');
//            }
//
////			String mapStr = null;
////			if(line.hasOption('a')){
////				mapStr = line.getOptionValue('a');
////				System.out.println("AsyncMap mapStr " + mapStr);
////				if(mapStr != null) {
////					String[] strs = mapStr.split(":");
////					asyncMap = new HashMap<>();
////					for(int i = 0; i < strs.length; i++) {
////						if(i % 2 == 1) {
////							System.out.println("AsyncMap key " + strs[i - 1] + " value " + strs[i]);
////							asyncMap.put(strs[i - 1], strs[i]);
////						}
////					}
////				}
////			}
//
//            ServerStart serverStart = new ServerStart(serverPort, maxThreadPoolSize, maxInnerThreadPoolSize, customWarPath);
////			serverStart.setAsyncServletMap(asyncMap);
//            serverStart.start();
//        }  catch (Exception e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//}
