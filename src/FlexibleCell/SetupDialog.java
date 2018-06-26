/*
    A basic extension of the java.awt.Dialog class 
 */ 
 
import java.awt.*; 
import java.io.*; 
import java.util.*; 
 
 
 
public class SetupDialog extends Dialog { 
 
 
	Hashtable itemIndex = null; 
 
 
 
 
 
 
 
 
	void itemList_DblClicked(Event event) { 
    	addButton_Clicked(event); 
 
	} 
 
	void saveButton_Clicked(Event event) { 
 
		FileDialog fileDialog = new FileDialog((Frame)getParent(),"Save Testfile",FileDialog.SAVE); 
		fileDialog.setFile("*.tst"); 
		fileDialog.show(); 
		String file = fileDialog.getFile(); 
		if (file != null) 
    		saveTstFile(file);				 
	} 
 
	void loadButton_Clicked(Event event) { 
 
		FileDialog fileDialog = new FileDialog((Frame)getParent(),"Load Testfile",FileDialog.LOAD); 
		fileDialog.setFile("*.tst"); 
		fileDialog.show(); 
		String file = fileDialog.getFile(); 
		if (file != null) 
    		loadTstFile(file);				 
	} 
 
	void okButton_Clicked(Event event) { 
 
 
		//{{CONNECTION 
		// Hide the Dialog 
		hide(); 
		//}} 
	} 
 
	void downButton_Clicked(Event event) { 
 
 
		//{{CONNECTION 
		// Add a string to the List... Get the current item text 
		int index = cycleList.getSelectedIndex(); 
		if (index >= (cycleList.countItems()-1)) 
		    return; 
 
        String toMoveDown = cycleList.getSelectedItem(); 
        String toMoveUp = cycleList.getItem(index+1); 
         
		 
		cycleList.replaceItem(toMoveUp,index); 
		cycleList.replaceItem(toMoveDown,index+1); 
		cycleList.select(index+1); 
		//}} 
	} 
 
	void upButton_Clicked(Event event) { 
 
 
		//{{CONNECTION 
		// Add a string to the List... Get the current item text 
		int index = cycleList.getSelectedIndex(); 
		if (index <= 0) 
		    return; 
 
        String toMoveUp = cycleList.getSelectedItem(); 
        String toMoveDown = cycleList.getItem(index-1); 
         
		 
		cycleList.replaceItem(toMoveUp,index-1); 
		cycleList.replaceItem(toMoveDown,index); 
		cycleList.select(index-1); 
		//}} 
	} 
 
	void remButton_Clicked(Event event) { 
 
 
		//{{CONNECTION 
		// Delete an item from the List... Get the current item index 
		int index = cycleList.getSelectedIndex(); 
		if (index != -1) { 
    		cycleList.delItem(index); 
    		int count = cycleList.countItems(); 
    		if  (count != 0) 
    		{ 
        		if (index == count) 
	            		cycleList.select(index-1); 
        		else 
       				cycleList.select(index); 
        	} 
    		 
    	} 
		//}} 
	} 
 
	void addButton_Clicked(Event event) { 
 
 
		//{{CONNECTION 
		// Add a string to the List... Get the current item text 
		String time = insAfterTF.getText().trim(); 
		if (!time.equals("") && itemList.getSelectedIndex()!=-1) { 
    		String toAdd = itemList.getSelectedItem(); 
	    	cycleList.addItem(toAdd+" : "+time); 
	    } 
		//}} 
	} 
 
	void SetupDialog_GotFocus(Event event) { 
		// to do: place event handler code here. 
	} 
 
 
	public SetupDialog(Frame parent, boolean modal) { 
 
	    super(parent, modal); 
	    //      		addNotify(); 

		//{{INIT_CONTROLS 
		setLayout(null); 
		resize(insets().left + insets().right + 447,insets().top + insets().bottom + 294); 
		itemList = new java.awt.List(0,false); 
		add(itemList); 
		itemList.reshape(insets().left + 12,insets().top + 24,126,240); 
		itemLabel = new java.awt.Label("Select Item:"); 
		itemLabel.reshape(insets().left + 12,insets().top + 0,108,24); 
		add(itemLabel); 
		addButton = new java.awt.Button("Add -->"); 
		addButton.reshape(insets().left + 144,insets().top + 36,60,24); 
		add(addButton); 
		remButton = new java.awt.Button("Remove"); 
		remButton.reshape(insets().left + 144,insets().top + 144,60,24); 
		add(remButton); 
		cycleList = new java.awt.List(0,false); 
		add(cycleList); 
		cycleList.reshape(insets().left + 216,insets().top + 24,132,240); 
		cycleLabel = new java.awt.Label("Items to insert:"); 
		cycleLabel.reshape(insets().left + 216,insets().top + 0,108,24); 
		add(cycleLabel); 
		upButton = new java.awt.Button("Move Up"); 
		upButton.reshape(insets().left + 360,insets().top + 36,72,24); 
		add(upButton); 
		downButton = new java.awt.Button("Move Down"); 
		downButton.reshape(insets().left + 360,insets().top + 72,72,24); 
		add(downButton); 
		loadButton = new java.awt.Button("Load Test"); 
		loadButton.reshape(insets().left + 360,insets().top + 156,72,24); 
		add(loadButton); 
		saveButton = new java.awt.Button("Save Test"); 
		saveButton.reshape(insets().left + 360,insets().top + 192,72,24); 
		add(saveButton); 
		okButton = new java.awt.Button("OK"); 
		okButton.reshape(insets().left + 360,insets().top + 240,72,24); 
		add(okButton); 
		insAfterTF = new java.awt.TextField(); 
		insAfterTF.setText("0.0"); 
		insAfterTF.reshape(insets().left + 144,insets().top + 96,48,24); 
		add(insAfterTF); 
		label1 = new java.awt.Label("s"); 
		label1.reshape(insets().left + 180,insets().top + 96,21,21); 
		add(label1); 
		label2 = new java.awt.Label("insert After"); 
		label2.reshape(insets().left + 132,insets().top + 72,77,21); 
		add(label2); 
		setTitle("Setup Test"); 
		setResizable(false); 
		//}} 
 
 
		itemIndex = new Hashtable(); 
	} 
 
	public SetupDialog(Frame parent, String title, boolean modal) { 
	    this(parent, modal); 
	    setTitle(title); 
	} 
 
    public synchronized void show() { 
    	Rectangle bounds = getParent().bounds(); 
    	Rectangle abounds = bounds(); 
 
    	move(bounds.x + (bounds.width - abounds.width)/ 2, 
    	     bounds.y + (bounds.height - abounds.height)/2); 
 
    	super.show(); 
    } 
 
	public boolean handleEvent(Event event) { 
	    if(event.id == Event.WINDOW_DESTROY) { 
	        hide(); 
	        return true; 
	    } 
		if (event.target == this && event.id == Event.GOT_FOCUS) { 
			SetupDialog_GotFocus(event); 
			return true; 
		} 
		if (event.target == addButton && event.id == Event.ACTION_EVENT) { 
			addButton_Clicked(event); 
			return true; 
		} 
		if (event.target == remButton && event.id == Event.ACTION_EVENT) { 
			remButton_Clicked(event); 
			return true; 
		} 
		if (event.target == upButton && event.id == Event.ACTION_EVENT) { 
			upButton_Clicked(event); 
			return true; 
		} 
		if (event.target == downButton && event.id == Event.ACTION_EVENT) { 
			downButton_Clicked(event); 
			return true; 
		} 
		if (event.target == okButton && event.id == Event.ACTION_EVENT) { 
			okButton_Clicked(event); 
			return true; 
		} 
		if (event.target == loadButton && event.id == Event.ACTION_EVENT) { 
			loadButton_Clicked(event); 
			return true; 
		} 
		if (event.target == saveButton && event.id == Event.ACTION_EVENT) { 
			saveButton_Clicked(event); 
			return true; 
		} 
		if (event.target == itemList && event.id == Event.ACTION_EVENT) { 
			itemList_DblClicked(event); 
			return true; 
		} 
		return super.handleEvent(event); 
	} 
	 
	 
    void loadTstFile(String fileName) 
    { 
        try { 
            FileInputStream stream = new FileInputStream(fileName); 
            DataInputStream input = new DataInputStream(stream); 
            cycleList.clear(); 
            try { 
                while (true) 
                { 
                    String inp = input.readLine(); 
                    if (inp != null) 
                        cycleList.addItem(inp); 
                    else 
                        input.close(); 
                } 
            } 
            catch (IOException e) 
            {                 
            } 
        }  
        catch (IOException e) 
        { 
            System.out.println("Some error has ocurred : "+e.getMessage()); 
        } 
    } 
 
    void saveTstFile(String fileName) 
    { 
        try { 
            FileOutputStream stream = new FileOutputStream(fileName); 
            PrintStream output = new PrintStream(stream); 
            int count = cycleList.countItems(); 
            int index; 
            for (index = 0;index < count; index++) 
            {   
                output.println(cycleList.getItem(index)); 
            } 
            output.close(); 
        } catch (IOException e) 
        { 
            System.out.println(e.getMessage()); 
        } 
         
    } 
 
    public void addItem(String name,Object item) 
    { 
	itemList.addItem(name); 
	itemIndex.put(name,item); 
	 
    } 
 
 
 
    public Vector getTestVector() 
    { 
	int count = cycleList.countItems(); 
	if (count == 0) 
	  return null; 
 
	Vector items = new Vector(count); 
 
        int index; 
	for (index = 0;index < count; index++) 
        {   
	   String listItem = cycleList.getItem(index); 
	   int dp = listItem.indexOf(':'); 
	   String item = (listItem.substring(0,dp).trim()); 
	   String time = (listItem.substring(dp+1).trim()); 
 
	   int itime = (int)((Double.valueOf(time)).doubleValue()*1000.0); 
	   items.addElement(new TestItem(itemIndex.get(item),itime)); 
	} 
 
	return items; 
    } 
 
 
 
 
	//{{DECLARE_CONTROLS 
	java.awt.List itemList; 
	java.awt.Label itemLabel; 
	java.awt.Button addButton; 
	java.awt.Button remButton; 
	java.awt.List cycleList; 
	java.awt.Label cycleLabel; 
	java.awt.Button upButton; 
	java.awt.Button downButton; 
	java.awt.Button loadButton; 
	java.awt.Button saveButton; 
	java.awt.Button okButton; 
	java.awt.TextField insAfterTF; 
	java.awt.Label label1; 
	java.awt.Label label2; 
	//}} 
} 
