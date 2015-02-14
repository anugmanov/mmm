/*
#Works with any version of LSBLK
#udisks for mounting
#Using DMENU 
#i3 for best experience
#---------------------------
#Author: __nugman           |
#SCRIPT FOR EASY MONITORING | 
#AND MOUNTING PURPOSES 	    |
#IN TILING WM's		    	|
#---------------------------
*/

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class mmm{
	List<Drive> ds = new ArrayList<Drive>();
	
	public static void main(String args[]) throws IOException{
		
		mmm obj = new mmm();
		
		ConfigFile CF = new ConfigFile();
		CF.ReadConfig();
		String[] nomount = CF.NomountDrives();
		
		obj.InitDrives(obj.execCmd(), nomount);
		
		String mnt = "";
		for(int i = 0;i<=obj.ds.size() - 1;i++){
			if (obj.ds.get(i).mounted){
			mnt += obj.ds.get(i).name  + ' ' + "DISMOUNT" + ' ' + '#' + ' ' + 
					obj.ds.get(i).size  + ' ' + '#' + ' ' +
					obj.ds.get(i).mountpoint + '\n';
			}
			else{
				mnt += obj.ds.get(i).name  + ' ' + '#' + ' ' + 
						obj.ds.get(i).size  + ' ' + '#' + ' ' +
						obj.ds.get(i).mountpoint + '\n';
			}
		}
		
		String ans = obj.execDmenu(mnt); //Run DMENU here and get user out
		if(ans.length() == 0) { System.exit(0); } 
		String[] UDrive = ans.split(" ");
		
		if(UDrive[1].equals("DISMOUNT")){
			obj.execUdisks(UDrive[0], false, CF.IsNotify()); //Finally DISMOUNT
		}
		else {obj.execUdisks(UDrive[0], true, CF.IsNotify()); } //Finally MOUNT
		System.exit(0);
	}
	
	
	public void execUdisks(String s, boolean mount, boolean notify){
		String[] cmd;
		if(mount){
			cmd = new String[]{
					"udisks",
					"--mount",
					"/dev/" + s};
		}
		else { cmd = new String[]{"udisks",
				"--unmount",
				"/dev/" + s };
		}
		
		StringBuffer res = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while((line = reader.readLine())!=null){
				res.append(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(notify){ //send notification
			if(!mount){
				cmd = new String[] {"notify-send",
						s + ' ' + "dismounted, Yay!" };
			}
			else {
				cmd = new String[] {
						"/usr/bin/notify-send",
						res.toString()};
			}
			try{
				p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	
	public String execDmenu(String s){
		//String cmd = ""
		/*String cmd = "echo -e " + s +
				"| dmenu -fn \"-xos4-terminus-*-*-*-*-12-*-*-*-*-*-*-*\" -i -l" + ' ' +
				String.valueOf(ds.size());//convert int->string and proccess the main command
				*/
		String[] cmd = { //FORM RES MAGIC STRING HERE
				"/bin/bash",
				"-c",
				"echo -e \"" + s + "\"" +
				"| dmenu -fn \"-xos4-terminus-*-*-*-*-12-*-*-*-*-*-*-*\" -i -l" + ' ' +
				String.valueOf(ds.size())
		};
		
		StringBuffer res = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while((line = reader.readLine())!=null){
				res.append(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return res.toString();
		
	}
	
	
	public StringBuffer execCmd(){
		String cmd = "lsblk -lnr -o name,size,type,mountpoint";//fixed bug with mountpoints
		StringBuffer res = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line= "";
			while((line = reader.readLine())!=null){//read lines from stdout of lsblk
				if(line.indexOf("/") == -1){ //if didn't mount - place NOMOUNT
					res.append(line + "NOMOUNT"+ ' ');
				}
				else { //replace all whitespaces with underscores - to fix whitespace-splitting
					String tmpline = line.substring(line.indexOf("/"),line.length()).replace(' ', '_');
					res.append(line.substring(0, line.indexOf("/")) + tmpline + ' ');
					tmpline = null; //free memory
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return res;
	}
	
	public void InitDrives(StringBuffer CmdRes, String[] nomount){
		CmdRes.append("1 2 3 4 5 6 7 8 9 0"); //dirty hax
		
		String[] drives = CmdRes.toString().split(" ");
		Drive tmp = new Drive();
		for(int i = 0;i<drives.length - 10;i+=4){
			/* Remove parent drives if they have children
			 * sda - parent drive (len == 3)
			 * sda1 - child drive*/
			if(drives[i].length()<3)continue;
			if(drives[i].length() == 3 && 
					(drives[i+4].indexOf(drives[i])!=-1))continue;
			
			boolean NeedToMount = true;
			if(nomount!=null){
				for(int j = 0; j<=nomount.length - 1;j++){ //See nomount drives
					
					if(drives[i].indexOf(nomount[j]) != -1){ //if need to exclude
						NeedToMount = false;
						break;
					}
					
				}	
			}
			
			
			if(NeedToMount){
				tmp.name = drives[i];
				tmp.size = drives[i + 1];
				if(drives[i+2] == "disk")tmp.disk = true;
				tmp.mountpoint = drives[i+3];
				if(!tmp.mountpoint.equals("NOMOUNT")) {
					tmp.mounted = true;
				}
					
				ds.add(tmp); //INITIALIZE array of devices
				tmp = new Drive();
			}
			
			}
		}
	}

class ConfigFile{
	private boolean dismount;
	private boolean notify;
	private String[] nomount;
	
	String HDir = System.getProperty("user.home");
	String CFile = HDir + "/.config/mmm.conf";
	
	ConfigFile(){
		dismount=true;
		notify = true;
	}
	
	public void ReadConfig() throws IOException{
		File f = new File(CFile);
		if(!f.exists()) { return; }
		
		BufferedReader in = new BufferedReader(new FileReader(CFile));
		
		try {
			String s;
			while((s=in.readLine())!=null){
				//parse config file here
				String[] action = s.split("=");
				
				switch(action[0]){
				case "no-mount":
					String[] drives = s.split("=");
					nomount = drives[1].split(",");
					break;
				case "no-notify":
					if(Integer.parseInt(action[1]) == 1) notify = false;
					break;
				case "dismount":
					if(Integer.parseInt(action[1]) == 0) dismount = false;
					break;
				}
			}
					
		} finally{
				in.close();
		}
	}
	public boolean IsDismount() {return dismount; }
	public boolean IsNotify() {return notify; }
	public String[] NomountDrives() {
		if(nomount != null)return nomount;
		else return null;
	}
}

class Drive{
	String name;
	String size;
	boolean disk;
	boolean mounted;
	String mountpoint;
	
	Drive(){
		disk=false;
		mounted = false;
	}
}