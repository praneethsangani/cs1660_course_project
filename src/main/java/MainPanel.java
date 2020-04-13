import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.model.HadoopJob;
import com.google.api.services.dataproc.model.Job;
import com.google.api.services.dataproc.model.JobPlacement;
import com.google.api.services.dataproc.model.SubmitJobRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainPanel extends JFrame implements ActionListener {
    private JTextArea loadedFiles = new JTextArea("");
    private JTextArea title = new JTextArea("Praneeth's Search Engine");
    private JTextArea loadMyEngine = new JTextArea("Load My Engine");
    private JButton chooseFilesButton = new JButton("Choose Files");
    private JButton constructInvertedIndices = new JButton("Construct Inverted Indices");
    private JFileChooser fileChooser = new JFileChooser();

    private static HttpRequestInitializer requestInitializer;
    private static GoogleCredentials credentials;
    private File[] files;

    private MainPanel() {
        Container contentPane = this.getContentPane();
        contentPane.setBackground(Color.WHITE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Praneeth's Search Engine");
        this.setSize(800, 500);
        this.setVisible(true);
        this.setVisible(true);
        this.setLayout(null);

        title.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        title.setBounds(0, 0, 180, 30);
        title.setEditable(false);

        loadedFiles.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        loadedFiles.setBounds(250, 200, 200, 100);
        loadedFiles.setEditable(false);
        loadedFiles.setLineWrap(true);
        loadedFiles.setWrapStyleWord(true);

        loadMyEngine.setFont(new Font("TimesRoman", Font.BOLD, 25));
        loadMyEngine.setBounds(250, 100, 200, 30);
        loadMyEngine.setEditable(false);

        chooseFilesButton.setBounds(250, 150, 200, 50);
        chooseFilesButton.addActionListener(this);
        constructInvertedIndices.setBounds(225, 300, 250, 50);
        constructInvertedIndices.addActionListener(this);

        contentPane.add(loadMyEngine);
        contentPane.add(loadedFiles);
        contentPane.add(title);
        contentPane.add(chooseFilesButton);
        contentPane.add(constructInvertedIndices);
    }

    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();

        if (button == chooseFilesButton) {
            selectFiles();
        } else if (button == constructInvertedIndices) {
            try {
                constructInvertedIndices();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void constructInvertedIndices() throws IOException {
        Dataproc dataproc = new Dataproc.Builder(new NetHttpTransport(), new JacksonFactory(), requestInitializer).setApplicationName("InvertedIndex").build();

        double rand = Math.random() * 10000;
        List<String> args = new ArrayList<>();
        args.add("gs://dataproc-staging-us-west1-1079873161681-xg8hpwav/Data");
        args.add("gs://dataproc-staging-us-west1-1079873161681-xg8hpwav/output" + rand);
        List<String> jars = new ArrayList<>();
        jars.add("gs://dataproc-staging-us-west1-1079873161681-xg8hpwav/JAR/invertedindex.jar");

        Job job = new Job().setPlacement(new JobPlacement().setClusterName("cluster-1e03"))
                .setHadoopJob(new HadoopJob()
                        .setMainClass("InvertedIndex")
                        .setJarFileUris(jars)
                        .setArgs(args));
        dataproc.projects().regions().jobs().submit("cs1660-term-project", "us-west1", new SubmitJobRequest().setJob(job)).execute();
    }

    private void uploadFiles() {
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        for (File file : files) {
            storage.create(
                    BlobInfo.newBuilder("dataproc-staging-us-west1-1079873161681-xg8hpwav", "Data/" + file.getName())
                    .build()
            );
        }
    }

    private void selectFiles() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        loadedFiles.setText("");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            files = fileChooser.getSelectedFiles();
            for (File file : files) {
                loadedFiles.append(file.getName() + "\n");
            }
            uploadFiles();
        } else {
            loadedFiles.append("File Selection Canceled");
        }
    }

    private static void authExplicit(String jsonPath) throws IOException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        requestInitializer = new HttpCredentialsAdapter(credentials);

        System.out.println("Buckets:");
        Page<Bucket> buckets = storage.list();
        for (Bucket bucket : buckets.iterateAll()) {
            System.out.println(bucket.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        authExplicit("C:\\Users\\Praneeth\\Downloads\\cs1660_course_project\\src\\main\\resources\\creds.json");
        new MainPanel();
    }
}