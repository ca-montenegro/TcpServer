import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

class TCPClient extends JFrame implements ActionListener, MouseListener {
	JPanel panel;
	JLabel title, subT, msg, error, servFiles, clienteFiles,downTime;
	Font font,labelfont;
	JTextField txt;
	JButton up, down;
	String dirName;
	Socket clientSocket;
	InputStream inFromServer;
	OutputStream outToServer;
	BufferedInputStream bis;
	PrintWriter pw;
	String name, file, path;
	String hostAddr;
	int portNumber;
	int c;
	int totalRecv = 0;
	int size = 9022386;
	JList<String> filelist, filelistCliente;
	String[] names = new String[10000];
	int len; // number of files on the server retrieved

	public TCPClient(String dir, String host, int port) {
		super("TCP CLIENT");

		// set dirName to the one that's entered by the user
		dirName = dir;

		// set hostAddr to the one that's passed by the user
		hostAddr = host;

		// set portNumber to the one that's passed by the user
		portNumber = port;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel(null);

		font = new Font("Roboto", Font.BOLD, 60);
		title = new JLabel("TCP CLIENT");
		title.setFont(font);
		title.setBounds(300, 50, 400, 50);
		panel.add(title);

		labelfont = new Font("Roboto", Font.PLAIN, 20);
		subT = new JLabel("Enter File Name :");
		subT.setFont(labelfont);
		subT.setBounds(100, 450, 200, 50);
		panel.add(subT);

		txt = new JTextField();
		txt.setBounds(400, 450, 500, 50);
		panel.add(txt);

		up = new JButton("Upload");
		up.setBounds(250, 550, 200, 50);
		panel.add(up);

		down = new JButton("Download");
		down.setBounds(550, 550, 200, 50);
		panel.add(down);

		error = new JLabel("");
		error.setFont(labelfont);
		error.setBounds(200, 650, 600, 50);
		panel.add(error);

		up.addActionListener(this);
		down.addActionListener(this);

		try {
			clientSocket = new Socket(hostAddr, portNumber);
			inFromServer = clientSocket.getInputStream();
			pw = new PrintWriter(clientSocket.getOutputStream(), true);
			outToServer = clientSocket.getOutputStream();
			ObjectInputStream oin = new ObjectInputStream(inFromServer);
			String s = (String) oin.readObject();
			System.out.println(s);

			len = Integer.parseInt((String) oin.readObject());
			System.out.println(len);

			String[] temp_names = new String[len];

			for(int i = 0; i < len; i++) {
				String filename = (String) oin.readObject();
				System.out.println(filename);
				names[i] = filename;
				temp_names[i] = filename;
			}

			// sort the array of strings that's going to get displayed in the scrollpane
			Arrays.sort(temp_names);

			servFiles = new JLabel("Files in the Server Directory :");
			servFiles.setBounds(350, 125, 400, 50);
			panel.add(servFiles);

			filelist = new JList<>(temp_names);
			JScrollPane scroll = new JScrollPane(filelist);
			scroll.setBounds(300, 200, 400, 200);

			panel.add(scroll);
			filelist.addMouseListener(this);

		} 
		catch (Exception exc) {
			System.out.println("Exception: " + exc.getMessage());
			error.setText("Exception:" + exc.getMessage());
			error.setBounds(300,125,600,50);
			panel.revalidate();
		}

		getContentPane().add(panel);
	}

    public void mouseClicked(MouseEvent click) {
        if (click.getClickCount() == 2) {
           String selectedItem = (String) filelist.getSelectedValue();
           txt.setText(selectedItem);
           panel.revalidate();
         }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == up) {
			try {
				name = txt.getText();

				FileInputStream file = null;
				BufferedInputStream bis = null;

				boolean fileExists = true;
				path = dirName + name;

				try {
					file = new FileInputStream(path);
					bis = new BufferedInputStream(file);
				} catch (FileNotFoundException excep) {
					fileExists = false;
					System.out.println("FileNotFoundException:" + excep.getMessage());
					error.setText("FileNotFoundException:" + excep.getMessage());
					panel.revalidate();
				}

				if (fileExists) {
					// send file name to server
					pw.println(name);

					System.out.println("Upload begins");
					error.setText("Upload begins");
					panel.revalidate();

					// send file data to server
					sendBytes(bis, outToServer);
					System.out.println("Completed");
					error.setText("Completed");
					panel.revalidate();

					boolean exists = false;
					for(int i = 0; i < len; i++){
						if(names[i].equals(name)){
							exists = true;
							break;
						}
					}

					if(!exists){
						names[len] = name;
						len++;
					}

					String[] temp_names = new String[len];
					for(int i = 0; i < len; i++){
						temp_names[i] = names[i];
					}

					// sort the array of strings that's going to get displayed in the scrollpane
					Arrays.sort(temp_names);

					// update the contents of the list in scroll pane
					filelist.setListData(temp_names);

					// close all file buffers
					bis.close();
					file.close();
					outToServer.close();
				}
			} 
			catch (Exception exc) {
				System.out.println("Exception: " + exc.getMessage());
				error.setText("Exception:" + exc.getMessage());
				panel.revalidate();
			}
		}
		else if (event.getSource() == down) {
			try {
				File directory = new File(dirName);

				if (!directory.exists()) {
					directory.mkdir();
				}
				boolean complete = true;
				byte[] data = new byte[size];
				name = txt.getText();
				file = new String("*" + name + "*");
				pw.println(file); //lets the server know which file is to be downloaded

				long start = System.nanoTime();    
				ObjectInputStream oin = new ObjectInputStream(inFromServer);
				String s = (String) oin.readObject();

				if(s.equals("Success")) {
					File f = new File(directory, name);
					FileOutputStream fileOut = new FileOutputStream(f);
					DataOutputStream dataOut = new DataOutputStream(fileOut);
					
					ProgressBar frame = new ProgressBar();
	                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	                frame.pack();
	                frame.setLocationRelativeTo(null);
	                frame.setVisible(true);
	                
	                
	                

					//empty file case
	                totalRecv = 0;
					while (complete) {
						c = inFromServer.read(data, 0, data.length);
						frame.setTotal(data.length);
						totalRecv += c;
						
						if (c == -1) {
							complete = false;
							frame.setVal(data.length);
							frame.revalidate();
							System.out.println("Completed");
							error.setText("Completed");
							panel.revalidate();

						} else {
							frame.setVal(totalRecv);
							frame.revalidate();
							dataOut.write(data, 0, c);
							dataOut.flush();
						}
					}
					//frame.dispose();
					fileOut.close();
					double elapsedTime = (double) ((System.nanoTime() - start)/(Math.pow(10, 6)));
					File ff = new File(dirName);
					ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
					String[] temp_names1 = new String[names.size()];
					int cont = 0;
					for(String name: names){
						temp_names1[cont++]=name;
					}
					// sort the array of strings that's going to get displayed in the scrollpane
					Arrays.sort(temp_names1);
					
					downTime = new JLabel("Time to download File: "+ elapsedTime + " ms");
					downTime.setBounds(350, 700, 400, 50);
					panel.add(downTime);
					
					clienteFiles = new JLabel("Files in the Client Directory:");
					clienteFiles.setBounds(350, 725, 400, 50);
					panel.add(clienteFiles);

					filelistCliente = new JList<>(temp_names1);
					JScrollPane scroll1 = new JScrollPane(filelistCliente);
					scroll1.setBounds(300, 800, 400, 200);

					panel.add(scroll1);
					filelistCliente.addMouseListener(this);
					panel.revalidate();
				}
				else {
					System.out.println("Requested file not found on the server.");
					error.setText("Requested file not found on the server.");
					panel.revalidate();
				}
			} 
			catch (Exception exc) {
				System.out.println("Exception: " + exc.getMessage());
				error.setText("Exception:" + exc.getMessage());
				panel.revalidate();
			}
		}
	}

	private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
		int size = 9022386;
		byte[] data = new byte[size];
		int bytes = 0;
		int c = in.read(data, 0, data.length);
		out.write(data, 0, c);
		out.flush();
	}

	public static void main(String args[]) {
		// if at least three argument are passed, consider the first one as directory path,
		// the second one as host address and the third one as port number
		// If host address is not present, default it to "localhost"
		// If port number is not present, default it to 3333
		// If directory path is not present, show error
		if(args.length >= 3){
			TCPClient tcp = new TCPClient(args[0], args[1], Integer.parseInt(args[2]));
			tcp.setSize(1000, 900);
			tcp.setVisible(true);
		}
		else if(args.length == 2){
			TCPClient tcp = new TCPClient(args[0], args[1], 3333);
			tcp.setSize(1000, 900);
			tcp.setVisible(true);
		}
		else if(args.length == 1){
			TCPClient tcp = new TCPClient(args[0], "localhost", 3333);
			tcp.setSize(1000, 900);
			tcp.setVisible(true);
		}
		else {
			System.out.println("Please enter the client directory address as first argument while running from command line.");
		}
	}
	
	public class ProgressBar extends JFrame {

	    JProgressBar current = new JProgressBar(0, 100);
	    int num = 0;
	    int total = 0;

	    public ProgressBar() {
	        //exit button
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        //create the panel to add the details
	        JPanel pane = new JPanel();
	        current.setValue(0);
	        current.setStringPainted(true);
	        pane.add(current);
	        setContentPane(pane);
	    }
	    
	    public void setVal(int val){
	    	int p = Math.round(((float)val / (float)total) * 100f);
	    	current.setValue(p);
	    	current.validate();
	    	
	    }
	    public void setTotal(int totl){
	    	total = totl;
	    }
	}
}
