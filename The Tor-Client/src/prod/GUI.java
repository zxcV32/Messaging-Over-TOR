package prod;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.msopentech.thali.java.toronionproxy.JavaOnionProxyContext;
import com.msopentech.thali.java.toronionproxy.JavaOnionProxyManager;
import com.msopentech.thali.java.toronionproxy.OnionProxyManager;
import com.msopentech.thali.java.toronionproxy.Utilities;

import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtOnion;
	private static JButton btnSend;
	private static JTextArea txtrMsg;
	private static String OnionAdress;
	private static JLabel lblsendingMessage;
	private static JTextArea textArea_1;
	private static OnionProxyManager onionProxyManager;
	private static String uid="";
	private static JTextArea textArea;
	/**
	 * Launch the application.
	 */
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
		
		JLabel lblChatWith = new JLabel("Chat with:");
		lblChatWith.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblChatWith.setBounds(12, 13, 130, 30);
		contentPane.add(lblChatWith);
		
		txtOnion = new JTextField();
		txtOnion.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtOnion.setToolTipText("blablabla.onion");
		txtOnion.setBounds(154, 13, 250, 30);
		contentPane.add(txtOnion);
		txtOnion.setColumns(10);
			
		lblsendingMessage = new JLabel("Sending message to: NaN");
		lblsendingMessage.setHorizontalAlignment(SwingConstants.LEFT);
		lblsendingMessage.setBounds(12, 60, 283, 16);
		contentPane.add(lblsendingMessage);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Client Logs", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(12, 94, 392, 358);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 26, 368, 319);
		panel.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "My Messages", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(414, 24, 354, 439);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 25, 330, 330);
		panel_1.add(scrollPane_1);
		
		textArea_1 = new JTextArea();
		textArea_1.setEditable(false);
		textArea_1.setLineWrap(true);
		scrollPane_1.setViewportView(textArea_1);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(12, 361, 250, 65);
		panel_1.add(scrollPane_2);
		
		txtrMsg = new JTextArea();
		scrollPane_2.setViewportView(txtrMsg);
		txtrMsg.setLineWrap(true);
		
		btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					send();
			}
		});
		btnSend.setEnabled(false);
		btnSend.setBounds(274, 401, 68, 25);
		panel_1.add(btnSend);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String temp=txtOnion.getText();
				try {
					if((temp.substring(temp.indexOf('.'))).equals(".onion") && temp.length() > 6) {
						OnionAdress=txtOnion.getText();
						lblsendingMessage.setText("Sending message to: "+OnionAdress);
						btnSend.setEnabled(true);
					}
					else
						throw new Exception();
				} catch (Exception e1) {
					textArea.append("Invalid .onion address\n");
					e1.printStackTrace();
				}
			}
		});

		btnSave.setBounds(307, 56, 97, 25);
		contentPane.add(btnSave);
		
		Thread client=new Thread(){
			public void run(){
				try {
					initializeClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		client.start();
		String CHAR_LIST = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuffer randStr = new StringBuffer(12);
		SecureRandom secureRandom = new SecureRandom();
		for( int i = 0; i < 8; i++ ) 
			randStr.append( CHAR_LIST.charAt( secureRandom.nextInt(CHAR_LIST.length()) ) );
		
		uid = randStr.toString();
		textArea.append("Building tor circuit...\n");
		textArea.append("My id: "+uid+"\n");
		System.out.println(uid);
	}

	private void initializeClient() throws IOException, InterruptedException {

		String fileStorageLocation = "torfiles_Client";
        onionProxyManager = new JavaOnionProxyManager(new JavaOnionProxyContext(new File(fileStorageLocation)));
        
        int totalSecondsPerTorStartup = 4 * 60;
        int totalTriesPerTorStartup = 5;
        // Start the Tor Onion Proxy
        if (onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup) == false) {
            return;
        }
        
	}
	
	private void send(){
        String temp=txtrMsg.getText();
		try {
			if(temp.length()>0) {
			    // Start a hidden service listener
				int hiddenServicePort = 80;
				int localPort = onionProxyManager.getIPv4LocalHostSocksPort();
				Socket clientSocket = Utilities.socks4aSocketConnection(OnionAdress, hiddenServicePort, "127.0.0.1", localPort);
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.flush();
				out.writeObject(uid+":"+txtrMsg.getText());
				out.flush();
				textArea_1.append("Sending to "+OnionAdress+" at "+(LocalDateTime.now()).toString()+"\n");
				textArea_1.append("Message>> "+txtrMsg.getText()+"\n");
				textArea_1.append("____________\n");
				txtrMsg.setText("");
			}
		} catch (Exception e) {
			textArea.append("could not send your last message\nPlease try again...\n");
		}
		
	}
}
