package tools;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FilePrinter {

    public static final String ACCOUNT_STUCK = "accountStuck.txt",
            EXCEPTION_CAUGHT = "exceptionCaught.txt",
            CLIENT_START = "clientStartError.txt",
            ADD_PLAYER = "addPlayer.txt",
            MAPLE_MAP = "mapleMap.txt",
            ERROR38 = "error38.txt",
            PACKET_LOG = "log.txt",
            EXCEPTION = "exceptions.txt",
            Acc_Stuck = "Acc_Stuck.txt",
            MapleClient_Disconect = "MapleClient_Erros.txt",
            PACKET_HANDLER = "PacketHandler/",
            ScriptEx_Log = "Log_Script_Except.rtf",
            PORTAL = "portals/",
            NPC = "npcs/",
            INVOCABLE = "invocable/",
            REACTOR = "reactors/",
            MISSOES = "missoes/",
            ITEM = "items/",
            QUEST = "quests/",
            MOB_MOVEMENT = "mobmovement.txt",
            Quest_Bug = "Quest_Bug.txt",
            PLAYER_MOVEMENT = "playermovement.txt",
            email = "email.txt",
            Timer_Log = "Log_Timer_Except.rtf",
            Map_Log = "Map_Log_Except.rtf",
            MAP_SCRIPT = "mapscript/",
            DIRECTION = "directions/",
            SAVE_CHAR = "saveToDB.txt",
            INSERT_CHAR = "insertCharacter.txt",
            LOAD_CHAR = "loadCharFromDB.txt",
            GMCommand_Log = "GM_Command_Log.rtf",
            SESSION = "sessoesiniciadas.txt";//more to come (maps)
    
            // Quest's
            
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static final String FILE_PATH = "Reports/Logs/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String emails = "Reports/Emails/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String primeiraEntrada = "Reports/NewPlayers/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String suporteGM = "Reports/GMSupport/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String JumpQuest = "Reports/JumpQuest/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printBanco = "Reports/Depositos/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printNovo = "Reports/NewPlayers/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String ERROR = "error/";
    private static final String printBug = "Reports/Bugs/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printHacker = "Reports/Hackers/Damage/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printHackerItemVac = "Reports/Hackers/ItemVac/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printIncubadora = "Reports/Incubator/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printCashPQ = "Reports/CashPQ/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printSiteMSG = "Reports/SiteMSG/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String printCashShop = "Reports/CashShop/" + sdf.format(Calendar.getInstance().getTime()) + "/";// + sdf.format(Calendar.getInstance().getTime()) + "/"
    
    
    public static void log(final String file, final String msg) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
	    out.write(msg.getBytes());
	    out.write("\n------------------------\n".getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}
    }
        public static final String CurrentReadable_Time() {
	return sdf.format(Calendar.getInstance().getTime());
    }
    
    
    public static void printError(final String name, final Throwable t) {
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(getString(t).getBytes());
            out.write("\n---------------------------------\r\n".getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void printError(final String name, final Throwable t, final String info) {
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write((info + "\r\n").getBytes());
            out.write(getString(t).getBytes());
            out.write("\n---------------------------------\r\n".getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void printError(final String name, final String s) {
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            //out.write("\n---------------------------------\n".getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void print(final String name, final String s) {
        print(name, s, true);
    }
    

    public static void print(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        String file = FILE_PATH + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("---------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printEmail(final String name, final String s) {
        printEmail(name, s, true);
    }
    
    public static void printEmail(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = emails + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("---------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printRelatorios(final String name, final String s) {
        printRelatorios(name, s, true);
    }
    
    public static void printRelatorios(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = primeiraEntrada + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    
    
    public static void printGM(final String name, final String s) {
        printGM(name, s, true);
    }
    
    public static void printGM(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = suporteGM + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printJumpQuest(final String name, final String s) {
        JumpQuest(name, s, true);
    }
    
    public static void JumpQuest(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = JumpQuest + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printSiteMSG(final String name, final String s) {
        printSiteMSG(name, s, true);
    }
    
    public static void printSiteMSG(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printSiteMSG + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printCashShop(final String name, final String s) {
        printCashShop(name, s, true);
    }
    
    public static void printCashShop(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printCashShop + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    
    
        public static void printNovo(final String name, final String s) {
        printNovo(name, s, true);
    }
    
    public static void printNovo(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printNovo + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
   public static void printBug(final String name, final String s) {
        printBug(name, s, true);
    }
    
    public static void printBug(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printBug + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    
    public static void printHacker(final String name, final String s) {
        printHacker(name, s, true);
    }
    
    public static void printHacker(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printHacker + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printHackerItemVac(final String name, final String s) {
        printHackerItemVac(name, s, true);
    }
    
    public static void printHackerItemVac(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printHackerItemVac + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    public static void printIncubadora(final String name, final String s) {
        printIncubadora(name, s, true);
    }
    
    public static void printIncubadora(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printIncubadora + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
        public static void printCashPQ(final String name, final String s) {
        printCashPQ(name, s, true);
    }
    
    public static void printCashPQ(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printCashPQ + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     public static void printBanco(final String name, final String s) {
        printBanco(name, s, true);
    }
    
    public static void printBanco(final String name, final String s, boolean line) {
        FileOutputStream out = null;
        final String file = printBanco + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("-------------------------------------------------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
     
    private static String getString(final Throwable e) {
        String retValue = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
            }
        }
        return retValue;
    }
}