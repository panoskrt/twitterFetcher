/*
 *   twitterFetcher 0.2
 *   Copyright (C) 2009  Panagiotis Kritikakos
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.PasswordAuthentication;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class twitterFetcher {

    public static void main(String[] args) {

        try {
            if ((args.length < 2) || (args.length > 4)) {
                System.out.println("Define the RSS Feed URL and the file where the tweets should be saved.");
                System.out.println("\n Fetch your own tweets: java twitterFetcher http://twitter.com/statuses/user_timeline/17578327.rss ~/Desktop/tweets");
                System.out.println("\n Or of those you follow: java twitterFetcher http://twitter.com/statuses/friends_timeline/17578327.rss ~/Desktop/tweets username password \n");
            } else {
                if (args.length == 2) {
                    readTweets(args[0].toString(), args[1].toString(), null, null);
                } else {
                    readTweets(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString());
                }
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
    }

    private static void readTweets(String feedURL, String fileName, String username, String password) {
        File tmpDir = new File("/tmp");
        String tempFile = "";
        if (tmpDir.exists()) {
            tempFile = "/tmp/twitterParserTMP";
        } else {
            tempFile = "C:\\temp\\twitterParserTMP";
        }

        File twitterFile = new File(fileName);
        boolean exists = false;
        if (twitterFile.exists()) {
            copyFile(fileName, tempFile);
            exists = true;
        }

        try {

            final String theUsername = username;
            final String thePassword = password;

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(theUsername, thePassword.toCharArray());
                }
            });

            URL xmlURL = new URL(feedURL.toString());

            FileWriter tweetsFile = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(tweetsFile);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlURL.openStream());
            doc.getDocumentElement().normalize();
            NodeList nodesList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodesList.getLength(); i++) {
                Node fstNode = nodesList.item(i);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element treeElement = (Element) fstNode;
                    Element treeNameElement;
                    NodeList elementList;
                    NodeList fieldList;

                    elementList = treeElement.getElementsByTagName("description");
                    treeNameElement = (Element) elementList.item(0);
                    fieldList = treeNameElement.getChildNodes();
                    String description = ((Node) fieldList.item(0)).getNodeValue();

                    elementList = treeElement.getElementsByTagName("pubDate");
                    treeNameElement = (Element) elementList.item(0);
                    fieldList = treeNameElement.getChildNodes();
                    String date = ((Node) fieldList.item(0)).getNodeValue();

                    out.write(date + " : " + description + "\n");
                }
            }
            out.close();
            if (exists) {
                uniqueTweets(fileName, tempFile);
            }
            System.out.println("\n [ Tweets saved ] \n");
        } catch (Exception ex) {
            System.out.println("Error while fetching the feed: " + ex);
        }
    }

    private static void uniqueTweets(String fileName, String tmpFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            Set<String> lines = new LinkedHashSet<String>(10000);
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();

            BufferedReader readerTMP = new BufferedReader(new FileReader(tmpFileName));
            while ((line = readerTMP.readLine()) != null) {
                lines.add(line);
            }
            readerTMP.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            for (String unique : lines) {
                writer.write(unique + "\n");
            }
            writer.close();
            File rmTemp = new File(tmpFileName);
            rmTemp.delete();
        } catch (Exception ex) {
            System.out.println("Error while sorting out unique tweets." + ex);
        }
    }

    private static void copyFile(String fileName, String tmpFile) {
        try {
            File inputFile = new File(fileName);
            File outputFile = new File(tmpFile);
            InputStream in = new FileInputStream(inputFile);
            OutputStream out = new FileOutputStream(outputFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception ex) {
            System.out.println("Error while copying the existing file: " + ex);
        }
    }
}
