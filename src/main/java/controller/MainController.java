package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import model.Destination;
import model.DrewPhotoMetadataExtractor;
import model.Photo;
import model.PhotoMetadata;
import model.PhotoMetadataExtractor;
import service.CopyJpgService;
import service.CopyRawService;
import service.CopyThumbService;

/**
 *
 * @author olivier MATHE
 */
public class MainController implements Initializable {

	@FXML
	private ComboBox<String> sourceDirectory;

	@FXML
	private TableView<Photo> photos;

	@FXML
	private TableView destinations;

	@FXML
	private AnchorPane leftPane;

	@FXML
	private TableColumn<Photo, Boolean> photoEnabledColumn;

	@FXML
	private TableColumn<Photo, String> photoNameColumn;

	@FXML
	private TableColumn<Photo, String> photoDateColumn;

	@FXML
	private TableColumn destinationEnabledColumn;

	@FXML
	private TableColumn destinationNameColumn;

	@FXML
	private TableColumn destinationRawColumn;

	@FXML
	private TableColumn destinationJpgColumn;

	@FXML
	private TableColumn destinationThumbColumn;

	@FXML
	private VBox copyBox;

	@FXML
	private Button copy;

	@FXML
	private Button cancelCopy;

	@FXML
	private Label photoCounter;

	@FXML
	private CheckBox recursiveAnalyse;

	//    private CopyThumbService copyThumbService;
	//    private CopyJpgService copyJpgService;
	//    private CopyRawService copyRawService;
	private PhotoMetadataExtractor metadataExtractor;

	private final ObservableList<Photo> data = FXCollections.observableArrayList();
	private final ObservableList<Destination> destinationsData = FXCollections.observableArrayList();

	Map<String, Service> services;
	//CopyJpgService copyJpgService;
	//CopyThumbService copyThumbService;
	CopyRawService copyRawService;

	public Map<String, Service> getServices() {
		return services;
	}

	public VBox getCopyBox() {
		return copyBox;
	}

	@Override
	public void initialize(URL location, final ResourceBundle resources) {

		sourceDirectory.setValue("/home/olivier/Téléchargements/test");
		destinationsData.add(new Destination(Boolean.TRUE, "USB Key", new File("/home/olivier/dest").toPath(), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));

		// photos
		photos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		photos.setItems(data);

		photoEnabledColumn.setCellFactory(object -> new CheckBoxTableCell());

		// date
		Callback<TableColumn<Photo, String>, TableCell<Photo, String>> photoDateCellFactory = new Callback<TableColumn<Photo, String>, TableCell<Photo, String>>() {

			@Override
			public TableCell<Photo, String> call(TableColumn<Photo, String> p) {
				return new PhotoDateCellFactory();
			}
		};
		photoDateColumn.setCellValueFactory(new PropertyValueFactory<Photo, String>("date"));
		photoDateColumn.setCellFactory(photoDateCellFactory);

		// destinations
		destinations.setItems(destinationsData);
		destinationRawColumn.setCellFactory(object -> new CheckBoxTableCell());
		destinationJpgColumn.setCellFactory(object -> new CheckBoxTableCell());
		destinationThumbColumn.setCellFactory(object -> new CheckBoxTableCell());
		destinationEnabledColumn.setCellFactory(object -> new CheckBoxTableCell());

		// bindings
		this.metadataExtractor = new DrewPhotoMetadataExtractor();

		services = new HashMap<String, Service>();

		//copyJpgService = new CopyJpgService();
		//copyThumbService = new CopyThumbService();
		copyRawService = new CopyRawService();
	}

	//    @FXML
	//    public void browseDestination(MouseEvent mouseEvent) {
	//        System.out.println("mouse clicked");
	//        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
	//            if (mouseEvent.getClickCount() == 2) {
	//                //System.out.println("Double clicked");
	//                DirectoryChooser directoryChooser = new DirectoryChooser();
	//                directoryChooser.setTitle("Sélection du répertoire de copie");
	//                File directory = directoryChooser.showDialog(null);
	//                if (directory != null) {
	//                    sourceDirectory.setValue(directory.getAbsolutePath());
	//                }
	//            }
	//
	//        } else {
	//            System.out.println("simple clicked");
	//        }
	//    }

	public void extract(Path path) {

		PhotoMetadata photoMetadata = new PhotoMetadata();
		try {

			//File file = new File("/media/DATA/dev/photo-archiver/src/test/resources/2015-11-01_16-51-17,082.jpg");
			Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
			ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (directory != null) {
				// date
				Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				if (date != null) {
					photoMetadata.setDate(date.getTime());
					System.out.println("date: " + date);
				}
				// camera
			}
			for (Directory dir : metadata.getDirectories()) {
				for (Tag tag : dir.getTags()) {
					System.out.println(tag.toString());
					//System.out.println(tag.getDirectoryName() + "," + tag.getTagType() + ", " + tag.getTagName() + ", " + tag.getDescription());
				}
			}

		} catch (ImageProcessingException | IOException ex) {
			Logger.getLogger(DrewPhotoMetadataExtractor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	protected void addDestination(ActionEvent event) {

		System.out.println("controller.MainController.addDestination()");
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("/media/DATA/copy"));
		directoryChooser.setTitle("Sélection du répertoire de copie");
		File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			destinationsData.add(new Destination(Boolean.TRUE, "USB Key", directory.toPath(), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
		}
	}

	@FXML
	protected void removeDestination(ActionEvent event) {

		Destination destination = (Destination) destinations.getSelectionModel().getSelectedItem();
		if (destination != null) {
			destinationsData.remove(destination);
		}
	}

	@FXML
	protected void selectDestination(MouseEvent event) {

		Destination destination = (Destination) destinations.getSelectionModel().getSelectedItem();
		if (destination != null) {
			System.out.println(destination);
		}
	}

	@FXML
	protected void copy(ActionEvent event) throws IOException {

		//copies
		destinationsData.stream().forEach(destination -> {
			try {
				destination.create();

				if (destination.getJpg()) {
					if (services.get(destination.getPath() + "jpg") == null) {
						FXMLLoader fxmlLoader = new FXMLLoader();
						HBox hBox = fxmlLoader.load(this.getClass().getResource("/copyBox.fxml").openStream());
						CopyBoxController copyBoxController = fxmlLoader.getController();
						copyBoxController.setCopyName("JPG: " + destination.getName());

						copyBox.getChildren().add(hBox);

						CopyJpgService jpgService = new CopyJpgService();
						jpgService.prepare(data.stream().filter(photo -> photo.getExtension().equalsIgnoreCase("jpg")), destination, "author");
						copyBoxController.setService(this, jpgService, destination.getPath() + "jpg", hBox);
						services.put(destination.getPath() + "jpg", jpgService);
						jpgService.start();

					}
				}
				if (destination.getThumb()) {
					if (services.get(destination.getPath() + "thumb") == null) {
						FXMLLoader fxmlLoader = new FXMLLoader();
						HBox hBox = fxmlLoader.load(this.getClass().getResource("/copyBox.fxml").openStream());
						CopyBoxController copyBoxController = fxmlLoader.getController();
						copyBoxController.setCopyName("THUMB: " + destination.getName());
						copyBox.getChildren().add(hBox);

						CopyThumbService thumbService = new CopyThumbService();
						thumbService.prepare(data.stream().filter(photo -> photo.getExtension().equalsIgnoreCase("jpg")), destination, "author");
						copyBoxController.setService(this, thumbService, destination.getPath() + "thumb", hBox);
						services.put(destination.getPath() + "thumb", thumbService);
						thumbService.start();
					}
				}
				if (destination.getRaw()) {
					if (services.get(destination.getPath() + "raw") == null) {
						FXMLLoader fxmlLoader = new FXMLLoader();
						HBox hBox = fxmlLoader.load(this.getClass().getResource("/copyBox.fxml").openStream());
						CopyBoxController copyBoxController = fxmlLoader.getController();
						copyBoxController.setCopyName("RAW: " + destination.getName());
						copyBox.getChildren().add(hBox);

						CopyRawService rawService = new CopyRawService();
						rawService.prepare(data.stream().filter(photo -> photo.getExtension().equalsIgnoreCase("cr2")), destination, "author");
						copyBoxController.setService(this, rawService, destination.getPath() + "raw", hBox);
						services.put(destination.getPath() + "raw", rawService);
						rawService.start();
					}
				}

			} catch (IOException ex) {
				Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
	}

	@FXML
	protected void cancelCopy(ActionEvent event) throws IOException {
		System.out.println("cancelCopy");
		//copyJpgService.cancel();
		copyRawService.cancel();
		//opyThumbService.cancel();

		//System.out.println("state jpg: " + copyJpgService.getState());
		System.out.println("state raw: " + copyRawService.getState());
		//System.out.println("state thumb: " + copyThumbService.getState());

		copyBox.getChildren().clear();

		/*for (Node node: copyBox.getChildren()) {
		    System.out.println("node id = " + node.getId());
		    System.out.println("node = " + node);
		    if (node.getId() != null && !node.getId().equals("copy") && !node.getId().equals("cancelCopy")) {
		        copyBox.getChildren().remove(node);
		        System.out.println("node id = " + node.getId() + " removed");
		    }
		}*/
		//copyBox.getChildren().clear();
		//copyBox.getChildren().addAll(copy, cancelCopy);
	}

	@FXML
	protected void selectAllPhotos(ActionEvent event) {

		data.stream().forEach(photo -> photo.setEnabled(true));
	}

	@FXML
	protected void deselectAllPhotos(ActionEvent event) {

		data.stream().forEach(photo -> photo.setEnabled(false));
	}

	@FXML
	public void browse(ActionEvent event) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Sélection du répertoire de photos");
		File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			sourceDirectory.setValue(directory.getAbsolutePath());
		}
	}

	@FXML
	protected void analyse(ActionEvent event) {

		File directory = new File(sourceDirectory.getValue().toString());

		data.clear();
		Stream<Path> files = null;
		try {

			if (recursiveAnalyse.isSelected()) {
				files = Files.walk(Paths.get(directory.toURI()))
						.filter(Files::isRegularFile);
			} else {
				files = Files.list(Paths.get(directory.toURI()));
			}
			files.forEach(
					path -> {
						if (path.toFile().isFile()) {
							//System.out.println(path);
							PhotoMetadata metadata = metadataExtractor.extract(path);
							//System.out.println(metadata);
							Photo photo = new Photo(true, path.getFileName().toString(), path, metadata);
							photo.formatName();
							data.add(photo);
						}
						//System.out.println(photo);
					});

		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			if (files != null) {
				files.close();
				displayPhotoCounter();
			}
		}
		//directory.list();
	}

	//	@FXML
	//	protected void analyse(ActionEvent event) {
	//		
	//		File directory = new File(sourceDirectory.getValue().toString());
	//		data.clear();
	//		Stream<Path> files = null;
	//		try {
	//			files = Files.list(Paths.get(directory.toURI()));
	//			files.forEach(
	//					path -> {
	//						if (path.toFile().isFile()) {
	//							//System.out.println(path);
	//							PhotoMetadata metadata = metadataExtractor.extract(path);
	//							//System.out.println(metadata);
	//							Photo photo = new Photo(true, path.getFileName().toString(), path, metadata);
	//							photo.formatName();
	//							data.add(photo);
	//						}
	//						//System.out.println(photo);
	//					});
	//			
	//		} catch (IOException ex) {
	//			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
	//		}
	//		finally {
	//			if (files != null) {
	//				files.close();
	//				displayPhotoCounter();
	//			}
	//		}
	//		//directory.list();
	//	}

	@FXML
	void clearPhotos(ActionEvent event) {
		data.clear();
		displayPhotoCounter();
	}

	private void displayPhotoCounter() {

		String text = "";
		if (data.isEmpty()) {
			photoCounter.setText("");
		} else if (data.size() == 1) {
			text = data.size() + " photo analysée";
		} else {
			text = data.size() + " photos analysées";
		}
		// photos with no date
		long noDateCounter = data.stream().filter(photo -> photo.getDate() == null).count();
		if (noDateCounter > 0) {
			if (noDateCounter == 1) {
				text += ", " + noDateCounter + " photo sans date";
			} else {
				text += ", " + noDateCounter + " photos sans date";
			}
		}
		photoCounter.setText(text);
	}
}
