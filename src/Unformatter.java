import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

public class Unformatter extends JFrame implements Runnable, ClipboardOwner {
	private static final long serialVersionUID = 6390754389743991748L;
	private static String contents;
	private static boolean pause = false;
	
	public Unformatter() {
		super();
	}
	
	public Unformatter(String title) {
		super(title);
	}
	
	public String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException ex){
				//highly unlikely since we are using a standard DataFlavor
				System.out.println(ex);
				ex.printStackTrace();
			}
			catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	* Place a String on the clipboard, and make this class the
	* owner of the Clipboard's contents.
	*/
	public void setClipboardContents(String aString) {
		StringSelection stringSelection = new StringSelection( aString );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(100);
				if(pause) {
					continue;
				}
				
				String newContents = getClipboardContents();
				if(newContents != null && !newContents.equals(contents)) {
					contents = newContents;
					contents = contents.replaceAll("\\s+", " ");
					contents = contents.replaceAll("(\r|\n)", "");
					setClipboardContents(contents);
				}
			}
			catch(Exception e) {}
		}
	};
	
	public static void main(String[] args) {
		final Unformatter uf = new Unformatter("Unformatter");
		Thread clipThread = new Thread(uf);
		
		Container content = uf.getContentPane();
		content.setLayout(new FlowLayout());
//		content.setLayout(new FlowLayout());
		ButtonGroup group = new ButtonGroup();
		JRadioButton onButton = new JRadioButton("On");
		onButton.setSelected(true);
		onButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pause = false;
			}
		});
		
		JRadioButton offButton = new JRadioButton("Off");
		offButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pause = true;
			}
		});
		
		group.add(onButton);
		group.add(offButton);
		content.add(onButton);
		content.add(offButton);
		
		JButton pButton = new JButton("<p>");
		pButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					uf.setClipboardContents("<p>" + uf.getClipboardContents() + "</p>");
				}
				catch(Exception e) {}
			}
		});
		content.add(pButton);
		
		uf.pack();
		uf.setVisible(true);
		uf.setDefaultCloseOperation(EXIT_ON_CLOSE);
		clipThread.start();
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
	}
}