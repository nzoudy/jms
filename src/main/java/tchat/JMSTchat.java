package tchat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JMSTchat extends Application{

	private MessageProducer messageProducer;
	private Session session;
	// private Session session2;
	private String codeUser;

	public static void main(String[] args) {
		Application.launch(JMSTchat.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("JMS Tchat");
		BorderPane borderPane = new BorderPane();
		HBox hbox = new HBox();

		hbox.setPadding(new Insets(10));
		hbox.setSpacing(10);
		hbox.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));

		Label labelCode = new Label("Code:");
		TextField textFieldCode = new TextField("C1");
		textFieldCode.setPromptText("Code");

		Label labelHost = new Label("Host");
		TextField textFieldHost = new TextField("localhost");
		textFieldHost.setPromptText("Host");

		Label labelPort = new Label("Port:");
		TextField textFieldPort = new TextField("61616");
		textFieldPort.setPromptText("Port");

		Button butttonConnecter = new Button("Connecter");

		hbox.getChildren().add(labelCode);
		hbox.getChildren().add(textFieldCode);
		hbox.getChildren().add(labelHost);
		hbox.getChildren().add(textFieldHost);
		hbox.getChildren().add(labelPort);
		hbox.getChildren().add(textFieldPort);
		hbox.getChildren().add(butttonConnecter);

		borderPane.setTop(hbox);

		VBox vBox = new VBox();
		GridPane gridPane = new GridPane();
		HBox hBox2 = new HBox();
		vBox.getChildren().add(gridPane);
		vBox.getChildren().add(hBox2);
		borderPane.setCenter(vBox);

		Label labelTo = new Label("To:");
		TextField textFieldTo = new TextField("C1"); textFieldTo.setPrefWidth(250);
		Label labelMessage = new Label("Message");
		TextArea textAreaMessage = new TextArea(); textAreaMessage.setPrefWidth(250);
		Button buttonEnvoyer = new Button("Envoyer");
		Label labelImage = new Label("Image:");

		File f = new File("images");
		ObservableList<String> observableListImages =
							FXCollections.observableArrayList(f.list());
		ComboBox<String> comboboxImages = new ComboBox<>(observableListImages);
		comboboxImages.getSelectionModel().select(0);
		Button buttonEnvoyerImage = new Button("Envoyer Image");

		gridPane.setPadding(new Insets(10));
		textAreaMessage.setPrefRowCount(2);
		gridPane.setVgap(10);
		gridPane.setHgap(10);
		gridPane.add(labelTo, 0, 0); gridPane.add(textFieldTo, 1, 0);
		gridPane.add(labelMessage, 0, 1); gridPane.add(textAreaMessage, 1, 1);
		gridPane.add(buttonEnvoyer, 2, 1);
		gridPane.add(labelImage, 0, 2);
		gridPane.add(comboboxImages, 1, 2);
		gridPane.add(buttonEnvoyerImage, 2, 2);


		ObservableList<String> observableListMessages =
				FXCollections.observableArrayList();
		ListView<String> listViewMessages = new ListView<>(observableListMessages);


		File f2 = new File("images/"+comboboxImages.getSelectionModel().getSelectedItem());
		Image image = new Image(f2.toURI().toString());
		ImageView imageVew = new ImageView(image);
		imageVew.setFitWidth(320); imageVew.setFitHeight(240);
		hBox2.getChildren().add(listViewMessages);
		hBox2.getChildren().add(imageVew);
		hBox2.setPadding(new Insets(10));
		hBox2.setSpacing(10);


		Scene scene = new Scene(borderPane, 800, 500);
		primaryStage.setScene(scene);
		primaryStage.show();


		comboboxImages.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				File f3 = new File("images/"+newValue);
				Image image = new Image(f3.toURI().toString());
				imageVew.setImage(image);
			}
		});

		buttonEnvoyer.setOnAction(e->{
			try {
				TextMessage textMessage = session.createTextMessage();
				textMessage.setText(textAreaMessage.getText());
				textMessage.setStringProperty("code", textFieldTo.getText());
				messageProducer.send(textMessage);


			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		buttonEnvoyerImage.setOnAction(e->{
			try {
				StreamMessage streamMessage = session.createStreamMessage();
				streamMessage.setStringProperty("code", textFieldTo.getText());

				File f4 = new File("images/"+comboboxImages.getSelectionModel().getSelectedItem());
				FileInputStream fis = new FileInputStream(f4);
				byte[] data=new byte[(int)f4.length()];
				fis.read(data);
				streamMessage.writeString(comboboxImages.getSelectionModel().getSelectedItem());
				streamMessage.writeInt(data.length);
				streamMessage.writeBytes(data);
				messageProducer.send(streamMessage);

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});
		butttonConnecter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				try {
					codeUser = textFieldCode.getText();
					String host = textFieldHost.getText();
					int port = Integer.parseInt(textFieldPort.getText());

					ConnectionFactory connectionFactory = new ActiveMQConnectionFactory
							("tcp://"+host+":"+port);
					Connection connection = connectionFactory.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					// session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

					Destination destination = session.createTopic("zz.chat");
					MessageConsumer messageConsumer=session.createConsumer(destination,"code='"+codeUser+"'");
					// MessageConsumer messageConsumer=session.createConsumer(destination);
					messageProducer = session.createProducer(destination);
					messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					messageConsumer.setMessageListener(message->{

							try {
								if(message instanceof TextMessage) {
									TextMessage textMessage = (TextMessage)message;
									observableListMessages.add(textMessage.getText());

								}
								else if(message instanceof StreamMessage) {
									StreamMessage streamMessage = (StreamMessage) message;
									String nomPhoto=streamMessage.readString();
									observableListMessages.add("Reception de la photo "+nomPhoto);
									int size = streamMessage.readInt();
									byte[] data = new byte[size];
									streamMessage.readBytes(data);
									ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
									Image image = new Image(byteArrayInputStream);
									imageVew.setImage(image);

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					});

					hbox.setDisable(true);

				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

}
