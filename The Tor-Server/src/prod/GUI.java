package prod;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.sf.T0rlib4j.controller.network.JavaTorRelay;
import net.sf.T0rlib4j.controller.network.TorServerSocket;

import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class GUI extends JFrame {

	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	static JTextArea lblLogslblLogs;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int hiddenservicedirport = 80;
    private static final int localport = 2096;
    private static CountDownLatch serverLatch = new CountDownLatch(2);
    public static String hiddenServiceAddress="";
    public static String hiddenServicePort="";
    private static JTextField txtHiddenServiceAddress;
    static JLabel lblPort;
    static JTextArea txtrMessage;
    private static LinkedList <String>uidList = new LinkedList<>();;
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 512);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Server Logs", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(12, 105, 459, 347);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 18, 441, 316);
		panel.add(scrollPane_1);
		
		lblLogslblLogs = new JTextArea();
		lblLogslblLogs.setEditable(false);
		scrollPane_1.setViewportView(lblLogslblLogs);
		lblLogslblLogs.setLineWrap(true);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Messages", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(483, 13, 287, 439);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 23, 263, 403);
		panel_1.add(scrollPane);
		
		txtrMessage = new JTextArea();
		txtrMessage.setEditable(false);
		txtrMessage.setLineWrap(true);
		scrollPane.setViewportView(txtrMessage);
		
		lblPort = new JLabel(":Port");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblPort.setHorizontalAlignment(SwingConstants.CENTER);
		lblPort.setBounds(364, 13, 107, 79);
		contentPane.add(lblPort);
		
		txtHiddenServiceAddress = new JTextField();
		txtHiddenServiceAddress.setEditable(false);
		txtHiddenServiceAddress.setFont(new Font("Tahoma", Font.PLAIN, 22));
		txtHiddenServiceAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtHiddenServiceAddress.setText("Hidden Service Address");
		txtHiddenServiceAddress.setBounds(12, 13, 340, 79);
		contentPane.add(txtHiddenServiceAddress);
		txtHiddenServiceAddress.setColumns(10);
		
		
		Thread server=new Thread(){
			public void run(){
				try {
					initializeServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		server.start();
	
	}

    static void initializeServer() throws IOException, InterruptedException, ClassNotFoundException, CloneNotSupportedException {
    	    	
        File fileStorageLocation = new File("torfiles_Server");

        lblLogslblLogs.append("Fetching tor hidden service address and bind port...\n");
        
        JavaTorRelay node = new JavaTorRelay(fileStorageLocation);
        TorServerSocket torServerSocket = node.createHiddenService(localport, hiddenservicedirport);

        hiddenServiceAddress=torServerSocket.getHostname();
        hiddenServicePort=Integer.toString(torServerSocket.getServicePort());
        
        txtHiddenServiceAddress.setText(hiddenServiceAddress);
        lblPort.setText(":"+hiddenServicePort);        
        lblLogslblLogs.append("Tor hidden service and port updated...\nBuilding Tor circuit...\n");
    	System.out.println("add"+hiddenServiceAddress);
		System.out.println("port"+hiddenServicePort);
        ServerSocket ssocks = torServerSocket.getServerSocket();
        Server server = new Server(ssocks);
        new Thread(server).start();
        
        serverLatch.await();
    }
     
    private static class Server implements Runnable {
        private final ServerSocket socket;
        private LocalDateTime now;

        private Server(ServerSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

        	lblLogslblLogs.append("Wating for incoming connections...\n");
          
              while (true) {
                try {
                    Socket sock = socket.accept();
                    this.now = LocalDateTime.now();
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    String temp = (String) in.readObject();
                    int col=temp.indexOf(':');
                    String incommingUid=temp.substring(0, col);
                    
                    if(!(uidList.contains(incommingUid)))
                    	uidList.add(incommingUid);
                    System.out.println(uidList.toString());
                    lblLogslblLogs.append("Incomming connection detected from: "+incommingUid+"\n");
                    lblLogslblLogs.append("Total unique connections: "+uidList.size()+"\n");
                   
                    txtrMessage.append("Client "+ incommingUid +" at " + dtf.format(now)+"\n");
                    txtrMessage.append("Message>> "+temp.substring(col+1)+"\n");
                    txtrMessage.append("____________\n");
                    sock.close();
                    lblLogslblLogs.append("Wating for incoming connections...\n");
                
            } catch (Exception e) {
            	lblLogslblLogs.append("Error receiving message...");
                e.printStackTrace();
            } 
            }
        }
    }
}
