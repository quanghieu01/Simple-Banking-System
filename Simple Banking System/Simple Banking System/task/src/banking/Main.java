package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static boolean logged = false;
    public static void main(String[] args) throws SQLException {
        String dbname=args[1];
        String command;
        String iduser="";
        Random rand = new Random();
        Scanner scanner = new Scanner(System.in);
        connectsqlite(dbname);


        do{
            if(logged==false) {
                System.out.println("1. Create an account\n2. Log into account\n0. Exit");
                command = scanner.next();
                if(command.equals("1")) {
                    int[] randdigits = new int[15];
                    String account = "400000";
                    int total =0;
                    randdigits[0]=4;
                    randdigits[1]=randdigits[2]=randdigits[3]=randdigits[4]=randdigits[5]=0;
                    for(int i=6;i<15;i++){
                        randdigits[i]=rand.nextInt(10);
                        account=account + String.valueOf(randdigits[i]);
                    }
                    for(int i=0;i<15;i++){
                        if(i%2==0) randdigits[i]=randdigits[i]*2;
                    }
                    for(int i=0;i<15;i++){
                        if(randdigits[i]>9) randdigits[i]=randdigits[i]-9;
                        total=total+randdigits[i];
                    }
                    if(total%10==0){
                        account=account+"0";
                    }else{
                        account=account+String.valueOf((total/10+1)*10-total);
                    }
                    String password = String.valueOf(rand.nextInt(9)+1)+String.valueOf(rand.nextInt(9)+1)+
                            String.valueOf(rand.nextInt(9)+1)+String.valueOf(rand.nextInt(9)+1);
                    try {
                        addsqlite(Integer.valueOf(account.substring(7)),account, password,0 ,dbname);
                        System.out.println("Your card has been created\nYour card number:");
                        System.out.println(account);
                        System.out.println("Your card PIN:");
                        System.out.println(password);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                }else if(command.equals("2")){
                    System.out.println("Enter your card number:");
                    String username = scanner.next();
                    System.out.println("Enter your PIN:");
                    String password = scanner.next();
                    if(readpassword(username,dbname).equals(password)){
                        logged = true;
                        iduser = username;
                        System.out.println("You have successfully logged in!");
                    }else{
                        System.out.println("Wrong card number or PIN!");
                    }
                }
            }else {
                System.out.println("1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Logout\n0. Exit");
                command = scanner.next();
                switch (command){
                    case "1":
                        System.out.println("Balance: "+readbalance(iduser,dbname));
                        break;
                    case "2":
                        System.out.println("Enter income:");
                        int income = scanner.nextInt();
                        updatebalance(iduser,readbalance(iduser,dbname)+income,dbname);
                        System.out.println("Income was added!");
                        break;
                    case "3":
                        System.out.println("Transfer");
                        System.out.println("Enter card number:");
                        String number = scanner.next();
                        if(number.equals(iduser)){
                            System.out.println("You can't transfer money to the same account!");
                        }else {
                            int[] numberdigit = new int[15];
                            int total =0;
                            int lastdigit;
                            for(int i=0;i<15;i++){
                                numberdigit[i] = Integer.valueOf(number.charAt(i)-'0');
                            }
                            for(int i=0;i<15;i++){
                                if(i%2==0) numberdigit[i]=numberdigit[i]*2;
                            }
                            for(int i=0;i<15;i++){
                                if(numberdigit[i]>9) numberdigit[i]=numberdigit[i]-9;
                                total=total+numberdigit[i];
                            }
                            if(total%10==0){
                                lastdigit = 0;
                            }else{
                                lastdigit = (total/10+1)*10-total;
                            }
                            if(lastdigit!=Integer.valueOf(number.charAt(15)-'0')){
                                System.out.println(lastdigit+"=="+Integer.valueOf(number.charAt(15)-'0'));
                                System.out.println("Probably you made a mistake in the card number. Please try again!");
                            }else {
                                if(readpassword(number,dbname)==""){
                                    System.out.println("Such a card does not exist.");
                                }else {
                                    System.out.println("Enter how much money you want to transfer:");
                                    int money = scanner.nextInt();
                                    if(money>readbalance(iduser,dbname)){
                                        System.out.println("Not enough money!");
                                    }else {
                                        updatebalance(iduser,readbalance(iduser,dbname)-money,dbname);
                                        updatebalance(number,readbalance(number,dbname)+money,dbname);
                                        System.out.println("Success!");
                                    }
                                }
                            }
                        }

                        break;
                    case "4":
                        deleteaccount(iduser,dbname);
                        Main.logged = false;
                        iduser = "";
                        System.out.println("The account has been closed!");
                        break;
                    case "5":
                        Main.logged = false;
                        iduser = "";
                        System.out.println("You have successfully logged out!");
                        break;

                }
            }
        }while(!command.equals("0"));
        System.out.println("Bye!");
    }

    public static void connectsqlite(String filename){
        String url = "jdbc:sqlite:./"+filename;//"jdbc:sqlite:C:/"

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL," +
                        "balance INTEGER DEFAULT 0)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int addsqlite(int id, String number, String pin, int balance, String db) throws SQLException {
        int i=0;
        String url = "jdbc:sqlite:./"+db;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                i = statement.executeUpdate("INSERT INTO card VALUES " +
                        "("+id+", "+number+", " +pin+ ", "+balance+")");
                System.out.println("INSERT INTO card VALUES " +
                        "("+id+", "+number+", " +pin+ ", "+balance+")");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return i;
    }

    public static int updatebalance(String number, int balance, String db) throws SQLException {
        int i=0;
        String url = "jdbc:sqlite:./"+db;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                i = statement.executeUpdate("UPDATE card SET balance = "+balance+" WHERE " +
                        "number ="+number);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return i;
    }

    public static int deleteaccount(String number, String db) throws SQLException {
        int i=0;
        String url = "jdbc:sqlite:./"+db;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                i = statement.executeUpdate("DELETE from card WHERE number = "+ number);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return i;
    }

//    public int updatesqlite(){
//
//    }
    public static String readpassword(String number, String db){
        String url = "jdbc:sqlite:./"+db;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                try(ResultSet user = statement.executeQuery("SELECT * FROM card WHERE number = "+number)){
                    return user.getString("pin");
                }catch (SQLException e){
                    e.printStackTrace();
                };
            } catch (SQLException e) {

            }
        } catch (SQLException e) {

        }
        return "";
    }
    public static int readbalance(String number, String db){
        String url = "jdbc:sqlite:./"+db;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                try(ResultSet user = statement.executeQuery("SELECT * FROM card WHERE number = "+number)){
                    return user.getInt("balance");
                }catch (SQLException e){
                    e.printStackTrace();
                };
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}