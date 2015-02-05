package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses scraped HTML pages from TigerBook and outputs a file:
 * {@code netid<tab>name<tab>imageURL}
 */
public class Parser {
	/** Search pattern for netids (\1). */
	private static final Pattern NETID_PATTERN = Pattern.compile(
		"<a href=\"student/([A-Za-z0-9_]{2,8})\">"
	);

	/** Search pattern for images (\1) and names (\2). */
	private static final Pattern IMG_PATTERN = Pattern.compile(
		"<img src=\"([^\"]+)\" title=\"([^\"]+)\" class=\"user-img\">"
	);

	private class Student {
		public String netId;
		public String name;
		public String url;
		
		public Student(String netId, String name, String url) {
			this.netId = netId;
			this.name = name;
			this.url = url;
		}
	}
	
	/** HTML directory (depth 1). */
	private File htmlDir;

	/** Output file (overwritten). */
	private File dest;
	
	/** Map of netid to Student objects. */
	private Map<String, Student> students;

	/**
	 * Constructor.
	 * @param htmlDir the HTML directory to parse
	 * @param dest the output file
	 */
	public Parser(File htmlDir, File dest) {
		this.htmlDir = htmlDir;
		this.dest = dest;
		if (!htmlDir.isDirectory()) {
			System.err.printf("No directory '%s' found.\n", htmlDir.getAbsolutePath());
			return;
		}
		
		this.students = new TreeMap<String, Student>();

		// look through all HTMLs
		File[] files = htmlDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".html");
			}
		});
		if (files.length < 1)
			return;

		// read individual files
		for (File file : files) {
			// strip non-results
			String html = stripNonResults(file);

			// find student netids
			Matcher netidMatcher = NETID_PATTERN.matcher(html);
			Matcher etcMatcher = IMG_PATTERN.matcher(html);
			while (netidMatcher.find()) {
				String netid = netidMatcher.group(1);
				etcMatcher.find();
				String imgUrl = etcMatcher.group(1);
				String name = etcMatcher.group(2);
				students.put(netid, new Student(netid, name, imgUrl));
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest)));
			for (String netid : students.keySet()) {
				Student s = students.get(netid);
				out.write(String.format("%s\t%s\t%s", s.netId, s.name, s.url));
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// panic
			e.printStackTrace();
		}
	}

	/**
	 * Returns a string containing the results in HTML, with other code stripped.
	 * @param file the HTML file
	 * @return the stripped string
	 */
	private String stripNonResults(File file) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			boolean inResults = false;
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (inResults) {
					if (line.equals("<!-- Pagination -->"))
						break;
					sb.append(line);
					sb.append('\n');
				} else {
					if (line.equals("<!-- Results Grid -->"))
						inResults = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Parser p = new Parser(new File("html"), new File("output.txt"));
		
	}
}
