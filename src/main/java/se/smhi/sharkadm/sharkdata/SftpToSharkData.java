//package sharkadm.sharkdata;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//
//import com.jcraft.jsch.Channel;
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//import com.jcraft.jsch.SftpException;
//
///**
// * Used to move datasets and resource files to the SHARKdata server.
// * 
// * Usage examples at the end of the file. 
// */
//public class SftpToSharkData {
//
//	private String sftpHost = "sharkdata.se";
//	private int sftpPort = 22;
//	private String sftpUser = "ftp-arnold";
//	private String sftpPwd = "filijoxen";
//	
//	private String sftpDatasetUpload = "/datasets_upload";
//	private String sftpDatasetTestUpload = "/datasets_test_upload";
//	private String sftpResourcesUpload = "/resources_upload";
//	private String sftpResourcesTestUpload = "/resources_test_upload";
//	
//    Session session = null;
//    Channel channel = null;
//    ChannelSftp channelSftp = null;
//	
//	public SftpToSharkData() {
//
//	}
//	
//	public void connectToSharkData() throws JSchException {
//        JSch jsch = new JSch();
//        // Session.
//        session = jsch.getSession(sftpUser, sftpHost, sftpPort);
//        session.setPassword(sftpPwd);
//        // Config.
//        java.util.Properties config = new java.util.Properties();
//        config.put("StrictHostKeyChecking", "no");
//        session.setConfig(config);
//        // Connect.
//        session.connect();
//        channel = session.openChannel("sftp");
//        channel.connect();
//        channelSftp = (ChannelSftp) channel;
//	}
//	
//	public void disconnect() {
//        channelSftp.exit();
//        channel.disconnect();
//        session.disconnect();		
//	}
//	
//	public void copyDatasetToSharkData(String fileNamePath, Boolean production) throws SftpException, FileNotFoundException {
//		if (production) {
//			channelSftp.cd(sftpDatasetUpload);
//		} else {
//			channelSftp.cd(sftpDatasetTestUpload);
//		}
//		try {
//			File f = new File(fileNamePath);
//			InputStream fileStream = new FileInputStream(f);
//			channelSftp.put(fileStream, f.getName(), ChannelSftp.OVERWRITE);
//			fileStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void removeDatasetFromSharkData(String fileNamePath, Boolean production) throws SftpException {
//		if (production) {
//			channelSftp.cd(sftpDatasetUpload);
//		} else {
//			channelSftp.cd(sftpDatasetTestUpload);
//		}
//		channelSftp.rm(fileNamePath);
//	}
//	
//	public void copyResourceToSharkData(String fileNamePath, Boolean production) throws SftpException, FileNotFoundException {
//		if (production) {
//			channelSftp.cd(sftpResourcesUpload);
//		} else {
//			channelSftp.cd(sftpResourcesTestUpload);
//		}
//		try {
//			File f = new File(fileNamePath);
//			InputStream fileStream = new FileInputStream(f);
//			channelSftp.put(fileStream, f.getName(), ChannelSftp.OVERWRITE);
//			fileStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void removeResourceFromSharkData(String fileNamePath, Boolean production) throws SftpException {
//		if (production) {
//			channelSftp.cd(sftpResourcesUpload);
//		} else {
//			channelSftp.cd(sftpResourcesTestUpload);
//		}
//		channelSftp.rm(fileNamePath);
//	}
//	
////	// TEST.
////	public static void main(String[] args) {
////		
////		SftpToSharkData sftpSharkData = new SftpToSharkData();
////		
////		try {
////			sftpSharkData.connectToSharkData();
////
////			String datasetFilePath = "D:\\arnold\\2_sharkadm\\aa-test-1.txt";
////			
////			sftpSharkData.copyDatasetToSharkData(datasetFilePath, true);
////			sftpSharkData.copyDatasetToSharkData(datasetFilePath, false);
////			
////			datasetFilePath = "aa-test-1.txt";
////			
////			sftpSharkData.removeDatasetFromSharkData(datasetFilePath, true);
////			sftpSharkData.removeDatasetFromSharkData(datasetFilePath, false);
////			
////			String resourceFilePath = "D:\\arnold\\2_sharkadm\\aa-test-2.txt";
////			
////			sftpSharkData.copyResourceToSharkData(resourceFilePath, true);
////			sftpSharkData.copyResourceToSharkData(resourceFilePath, false);
////			
////			resourceFilePath = "aa-test-2.txt";
////			
////			sftpSharkData.removeResourceFromSharkData(resourceFilePath, true);
////			
////			// Test wildcard.
////			resourceFilePath = "aa-*.txt";
////			
////			sftpSharkData.removeResourceFromSharkData(resourceFilePath, false);
////			
////			sftpSharkData.disconnect();
////		
////		} catch (FileNotFoundException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		} catch (JSchException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		} catch (SftpException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////	}
//
//}
