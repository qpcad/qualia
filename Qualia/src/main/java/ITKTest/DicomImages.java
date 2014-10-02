package ITKTest;

import org.itk.itkcommon.itkImageSS3;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.itk.itkiogdcm.itkGDCMSeriesFileNames;
import org.itk.itkioimagebase.itkImageSeriesReaderISS3;

/**
 * <pre>
 * kr.qualia
 * DicomImages.java
 * DICOM 파일 및 디렉토리 로드
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 * @version 
 * 
 * 
 */
public class DicomImages {
	private itkGDCMSeriesFileNames dicomNames_;
	
	/**
	 * <pre>
	 * 1.개요 : DICOM 디렉토리를 검색,
	 * 2.처리내용 : 입력 path에서 DICOM 디렉토리 검색하여 Series ID 리스트 생성
	 * </pre>
	 *
	 * @method scanDicomDirectory
	 * @param path 검색하고자하는 DICOM 디렉토리
	 * @return 검색된 Series ID 리스트
	 */
	public String[] scanDicomDirectory(String path) {
		// scan
		dicomNames_ = new itkGDCMSeriesFileNames();

		dicomNames_.SetRecursive(true);
		dicomNames_.SetUseSeriesDetails(true);
		dicomNames_.SetDirectory(path);
		
		String[] UIDs = dicomNames_.GetSeriesUIDs();
		
		return UIDs;
	}
	
	/**
	 * <pre>
	 * 1.개요 : 특정 Series에 포함되어 있는 영상파일 리스트 반환,
	 * 2.처리내용 : 선택된 Series의 UID를 입력하여 포함된 모든 영상파일의 리스트 반환
	 * </pre>
	 *
	 * @method getDicomNames
	 * @param UID 선택된 Series ID
	 * @return 영상파일 이름 리스트
	 */
	public String[] getDicomNames(String UID)
	{
		String[] names = dicomNames_.GetFileNames(UID);
		
		return names;
	}
	
	/**
	 * <pre>
	 * 1.개요 : DICOM image를 로드,
	 * 2.처리내용 : 영상파일 이름 리스트를 전달받아 itkImageSS3 클래스로 로드
	 * </pre>
	 *
	 * @method loadDicomImages
	 * @param names 영상파일 이름 리스트
	 * @return DICOM 영상 데이터
	 */
	public itkImageSS3 loadDicomImages(String[] names) {		
		// read
		itkImageSeriesReaderISS3 reader = new itkImageSeriesReaderISS3();
		itkGDCMImageIO dicomIO = new itkGDCMImageIO();
		
		reader.SetFileNames(names);
		reader.SetImageIO(dicomIO);
		reader.Update();
		
		itkImageSS3 lungImage = reader.GetOutput();
		lungImage.SetMetaDataDictionary(dicomIO.GetMetaDataDictionary());
				
		return lungImage;
	}
}
