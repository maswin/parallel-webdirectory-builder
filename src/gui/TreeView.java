package gui;



import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;
import javax.swing.JScrollPane;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Node;
//new TreeView(root).setVisible(true);
public class TreeView extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public TreeView(Cluster topicTreeRoot) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 400, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		

		DefaultMutableTreeNode root = generateTree(topicTreeRoot);

		JTree tree = new JTree(root);

		scrollPane.setViewportView(tree);

		
	}

	private DefaultMutableTreeNode generateTree(Cluster topicTreeRoot) {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				"Cluster "+topicTreeRoot.nodeID);
		if(topicTreeRoot.getChildren() != null && topicTreeRoot.getChildren().size()>0){				
			for(Node child : topicTreeRoot.getChildren()){
				root.add(generateTree((Cluster) child));
			}
		}else{
			DefaultMutableTreeNode tmp;
			StringTokenizer st = new StringTokenizer(topicTreeRoot.files.toString(),";");
			while(st.hasMoreTokens()){
				tmp = new DefaultMutableTreeNode(
						st.nextToken());
				root.add(tmp);
			}
		}
		
		
		return root;
	}
	

	
}