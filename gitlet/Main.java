package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Xiaoru Zhao
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        Command com = new Command();
        if (args[0].equals("init")) {
            com.init();
            return;
        }
        if (args[0].equals("add")) {
            com.add(args[1]);
            return;
        }
        if (args[0].equals("commit")) {
            try {
                if (args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    return;
                }
                com.commit(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a commit message.");
            }
            return;
        }
        if (args[0].equals("log")) {
            com.log();
            return;
        }
        if (args[0].equals("checkout")) {
            if (args.length == 2) {
                com.checkBranch(args[1]);
            } else if (args[1].equals("--")) {
                com.checkout(args[2]);
            } else if (args[2].equals("++")) {
                System.out.println("Incorrect operands.");
            } else {
                com.checkout(args[1], args[3]);
            }
            return;
        }
        if (helper(args)) {
            return;
        }
        System.out.println("No command with that name exists.");
    }

    /** Helper of main  method.
     *
     * @param args Input.
     * @throws IOException
     * @return whether the command is processed.
     */
    public static boolean helper(String... args) throws IOException {
        Command com = new Command();
        if (args[0].equals("global-log")) {
            com.globalLog();
            return true;
        }
        if (args[0].equals("status")) {
            com.status();
            return true;
        }
        if (args[0].equals("find")) {
            try {
                com.find(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a find message.");
            }
            return true;
        }
        if (args[0].equals("branch")) {
            try {
                com.branch(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a branch name.");
            }
            return true;
        }
        if (args[0].equals("rm")) {
            try {
                com.rm(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a file name.");
            }
            return true;
        }
        if (args[0].equals("rm-branch")) {
            try {
                com.rmBranch(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a branch name.");
            }
            return true;
        }
        if (args[0].equals("reset")) {
            try {
                com.reset(args[1]);
            } catch (NullPointerException exception) {
                System.out.println("Please enter a commit id.");
            }
            return true;
        }
        return false;
    }

}
