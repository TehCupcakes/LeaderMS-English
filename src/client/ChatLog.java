/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor. Then suck a cock.
 */

package client;

/**
 *
 * @author David
 
public class ChatLog {
	private String fileName;
	
	public static class ChatEntry {
		private Date time;
		private String msg;
		
		public ChatEntry(String msg) {
			this.time = new Date();
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public Date getTime() {
			return time;
		}

		public void setTime(Date time) {
			this.time = time;
		}
	}
	
	private List<String> chatLog = new LinkedList<String>();
	
	private ChatLog() {}
	
	public static ChatLog load(String charName) {
		ChatLog ret = new ChatLog();
		ret.fileName = charName;
		
		try {
			File root = new File("ChatLog/");
			if (!root.exists() || !root.isDirectory()) {
				root.mkdir();
			}
			File fl = new File("ChatLog/" + charName + ".log");
			if (!fl.exists()) {
				fl.createNewFile();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));
			String ln = null;
			while ((ln = br.readLine()) != null) {
				ret.chatLog.add(ln);
			}
			br.close();
		} catch (IOException ex) {
			Logger.getLogger(ChatLog.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ret;
	}
	
	public void save() {
		BufferedWriter out = null;
		try {
			File flog = new File("ChatLog/" + fileName + ".log");
			out = new BufferedWriter(new FileWriter(flog));
			PrintWriter pw = new PrintWriter(out);
			for (String s : chatLog) {
				pw.println(s);
			}
		} catch (IOException ex) {
			Logger.getLogger(ChatLog.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
				Logger.getLogger(ChatLog.class.getName()).log(Level.SEVERE, null, ex);
			}
		}		
	}
	
	public void log(ChatEntry ce) {
		chatLog.add("[" + DateFormat.getInstance().format(ce.getTime()) + "] " + ce.getMsg());
	}
}
*/