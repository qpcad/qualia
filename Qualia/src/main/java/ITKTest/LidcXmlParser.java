package ITKTest;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkIndex3;
import org.itk.itkimagegrid.itkSliceBySliceImageFilterIUC3IUC3;
import org.itk.itklabelmap.itkBinaryFillholeImageFilterIUC2;
import org.itk.itklabelmap.itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3;
import org.itk.itklabelmap.itkLabelMap3;
import org.itk.itklabelmap.itkLabelMapToBinaryImageFilterLM3IUC3;
import org.itk.itklabelmap.itkStatisticsLabelObjectUL3;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <pre>
 * kr.qualia
 * LidcXmlParser.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 * @version 
 */
public class LidcXmlParser {
	private itkImageSS3 lungImage_;
	private String xmlFilePath_;
	private itkLabelMap3[] noduleMap_;
	
	/**
	 * @param xmlFilePath
	 * @param lungImage
	 * @throws Exception
	 */
	public LidcXmlParser(String xmlFilePath, itkImageSS3 lungImage) throws Exception {
		lungImage_ = lungImage;
		noduleMap_ = new itkLabelMap3[4];
		xmlFilePath_ = xmlFilePath;
	}

	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method parseXML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parseXML() throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilder builder;
		DocumentBuilderFactory factory;
		Document document;
		
		factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		builder = factory.newDocumentBuilder();
		document = builder.parse(xmlFilePath_);
		
		Element root = document.getDocumentElement();
		NodeList sessions = root.getElementsByTagName("readingSession");
		
		System.out.println(sessions.getLength());
		
		// four investigators
		for(int si = 0; si < sessions.getLength(); si++) {
			Node session = sessions.item(si);
			NodeList noduleNodeList = ((Element)session).getElementsByTagName("unblindedReadNodule");
			
			itkLabelMap3 noduleMap = getNoduleMap(noduleNodeList);
			
			this.noduleMap_[si] = noduleMap;
			
			System.out.println("Objects " + noduleMap.GetNumberOfLabelObjects());

			String fileName = "/Users/taznux/Desktop/rnodule" + si + ".mha";
			
			ImageProcessingUtils.writeLabelMapOverlay(noduleMap, lungImage_, fileName);
		}
	}

	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method getNoduleMap
	 * @param nodeList
	 * @return
	 */
	private itkLabelMap3 getNoduleMap(NodeList nodeList) {
		itkLabelMap3 initialNoduleMap = new itkLabelMap3();
		
		initialNoduleMap.CopyInformation(lungImage_);
		
		for(int ni = 0; ni < nodeList.getLength(); ni++) {
			Node noduleNode = nodeList.item(ni);
			NodeList idNodeList = ((Element)noduleNode).getElementsByTagName("noduleID");
			String noduleId = idNodeList.item(0).getTextContent(); // TODO label and nodule id to be corresponded
			Nodule nodule = new Nodule();
			
			NodeList characteristicsNodeList = ((Element)noduleNode).getElementsByTagName("characteristics");
			if(characteristicsNodeList.getLength() > 0)
				getNoduleCharacteristics((Element)characteristicsNodeList.item(0), nodule);
			
			NodeList roisNodeList = ((Element)noduleNode).getElementsByTagName("roi");
			
			itkStatisticsLabelObjectUL3 noduleLabelObj = getNoduleGeometry(roisNodeList);
			noduleLabelObj.SetLabel(ni+1); // 0 is background
			
			initialNoduleMap.AddLabelObject(noduleLabelObj);
			
			nodule.setNoduleObj(noduleLabelObj);
			nodule.setNoduleUID(noduleId);
		}
		
		initialNoduleMap.Update();
		
		// hole filling
		itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBin = new itkLabelMapToBinaryImageFilterLM3IUC3();
		itkSliceBySliceImageFilterIUC3IUC3 sliceBySlice = new itkSliceBySliceImageFilterIUC3IUC3();
		itkBinaryFillholeImageFilterIUC2 fillHole = new itkBinaryFillholeImageFilterIUC2();
		itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3 noduleMapFilter = new itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3();
		
		labelMapToBin.SetInput(initialNoduleMap);
		sliceBySlice.SetInput(labelMapToBin.GetOutput());
		sliceBySlice.SetFilter(fillHole);
		noduleMapFilter.SetInput1(sliceBySlice.GetOutput());
		noduleMapFilter.SetInput2(lungImage_);
		noduleMapFilter.SetFullyConnected(false);
		
		noduleMapFilter.Update();
		
		// label matching to initial map
		itkLabelMap3 noduleMap = noduleMapFilter.GetOutput();
		long labels = noduleMap.GetNumberOfLabelObjects();
		for(int i = 1; i <= labels; i++) {
			itkIndex3 idx = noduleMap.GetLabelObject(i).GetIndex(0);			
			long label = initialNoduleMap.GetLabelObject(idx).GetLabel();			
			itkIndex3 idx2 = initialNoduleMap.GetLabelObject(idx).GetIndex(0);
			
			System.out.println(label + ":" + idx.GetElement(0) +","+ idx.GetElement(1) +","+ idx.GetElement(2) + ":" +idx2.GetElement(0) +","+ idx2.GetElement(1) +","+ idx2.GetElement(2) );
		}
		return noduleMap;
	}

	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method getNoduleCharacteristics
	 * @param element
	 * @param nodule
	 */
	private void getNoduleCharacteristics(Element element, Nodule nodule) {
		nodule.subtlety = Integer.parseInt(element.getElementsByTagName("subtlety").item(0).getTextContent());
		nodule.internalStructure = Integer.parseInt(element.getElementsByTagName("internalStructure").item(0).getTextContent());
		nodule.calcification = Integer.parseInt(element.getElementsByTagName("calcification").item(0).getTextContent());
		nodule.margin = Integer.parseInt(element.getElementsByTagName("margin").item(0).getTextContent());
		nodule.lobulation = Integer.parseInt(element.getElementsByTagName("lobulation").item(0).getTextContent());
		nodule.spiculation = Integer.parseInt(element.getElementsByTagName("spiculation").item(0).getTextContent());
		nodule.texture = Integer.parseInt(element.getElementsByTagName("texture").item(0).getTextContent());
		nodule.malignancy = Integer.parseInt(element.getElementsByTagName("malignancy").item(0).getTextContent());
}

	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method getNoduleGeometry
	 * @param nodeList
	 * @return
	 */
	private itkStatisticsLabelObjectUL3 getNoduleGeometry(NodeList nodeList) {
		itkStatisticsLabelObjectUL3 noduleLabelObj = new itkStatisticsLabelObjectUL3();
		
		for(int ri = 0; ri < nodeList.getLength(); ri++){
			Element roiElement = (Element)nodeList.item(ri);
		    NodeList xCoord = roiElement.getElementsByTagName("xCoord");
		    NodeList yCoord = roiElement.getElementsByTagName("yCoord");
		    String zPos = roiElement.getElementsByTagName("imageZposition").item(0).getTextContent();
		    String uId = roiElement.getElementsByTagName("imageSOP_UID").item(0).getTextContent();
		    String inc = roiElement.getElementsByTagName("inclusion").item(0).getTextContent();
		    
		    if(inc.compareToIgnoreCase("FALSE") == 0) continue;

		    int roiSize = xCoord.getLength();
		    System.out.print(uId + ":" + zPos + "-");
		    for(int ci=0;ci<roiSize;ci++) {
		    	itkIndex3 index = new itkIndex3();
		        int x = Integer.parseInt(xCoord.item(ci).getTextContent().trim());
		        int y = Integer.parseInt(yCoord.item(ci).getTextContent().trim());
		        int z = zPositionToIndex(Double.parseDouble(zPos));
		        
		        index.SetElement(0, x);
		        index.SetElement(1, y);
		        index.SetElement(2, z);
		        
		        noduleLabelObj.AddIndex(index);
		        
		        System.out.print("(" + x + ", " + y + ", " + z + ") ");
		    }

		    System.out.println();
		}
		
		return noduleLabelObj;
	}
	
	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method zPositionToIndex
	 * @param zPosition
	 * @return
	 */
	private int zPositionToIndex(double zPosition) {
		int zIndex = (int)((zPosition - lungImage_.GetOrigin().GetElement(2))/lungImage_.GetSpacing().GetElement(2));
		
		return zIndex;
	}
}

