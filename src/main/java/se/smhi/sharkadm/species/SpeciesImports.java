// package sharkadm.species;

// import java.io.BufferedReader;
// import java.util.List;

// import org.eclipse.swt.SWT;
// import org.eclipse.swt.widgets.MessageBox;
// import org.eclipse.swt.widgets.Shell;

// import sharkadm.species.TrophicTypeObject;
// import sharkadm.facades.ModelFacade;
// import sharkadm.utils.ParseFileUtil;

//public class SpeciesImports {
//
//	
//	
//	public void importFiles(String zipFileName) {
//		List<String[]> fileContent;
//		BufferedReader bufferedReader;
//		
//		try {
//			bufferedReader = ParseFileUtil.GetSharkConfigFile("species_trophic_type.txt");
//			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
//			if (fileContent != null) {				
//				importSpeciesTrophicTypeList(fileContent);
//			}
//		} catch (Exception e) {
//			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
//			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
//			messageBox.setText("Trophic type import");
//			messageBox.setMessage("Failed to import trophic type list. Error: " + e.getMessage());
//			messageBox.open();
//		}
//	}
//	
//	private void importSpeciesTrophicTypeList(List<String[]> fileContent) {
//		String[] header = null;
////		setExpectedColumns(stationHeader);
//		
////		int rowCounter = 1;
////		int addedItems = 0;
////		for (String[] row : fileContent) {
////			if (header == null) { 
////				// The first line contains the header.
////				header = row;
////				checkHeader(header);
////			} else {
////				rowCounter++;				
////				// Used columns:
////				// - scientific_name
////				// - size_class
////				// - trophic_type
////
////				TrophicTypeObject trophicTypeObject = new TrophicTypeObject();
////				trophicTypeObject.setScientific_name(getCell(row, "scientific_name"));
////				trophicTypeObject.setSize_class(getCell(row, "size_class"));
////				trophicTypeObject.setTrophic_type(getCell(row, "trophic_type"));
////								
////				SpeciesManager.instance().addTrophicType(trophicTypeObject);
////
////				addedItems++;
////			}
////		}
////		System.out.println("INFO: Added trophic types (scientific_name/size_class): " + addedItems + ".");
//	}
//
//
//	
//}
//
//
