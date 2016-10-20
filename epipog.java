import javafx.util.Pair;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.text.ParseException;

// Main entry for Query application
public class epipog {
	final static String usage = "Usage: epipog <options>\r\n" +
								"\t-c cache\t# size of cache in MB [not implemented]\r\n" +
								"\t-C collection\t# name of the data collection\r\n" +
								"\t-d datastore\t# type of data store (binary,psv,json,csv)\r\n" +
								"\t-f filter\t# filter [not implemented]\r\n" +
								"\t-F format\t# input format (psv,csv,tsv)\r\n" +
								"\t-i inputfile\t# import input file\r\n" +
								"\t-I index\t# index type (linked,binary)\r\n" +
								"\t-n\t\tno header in input file (e.g., csv, psv, tsv)\t\n" +
								"\t-o field(s)\t# order by fields\r\n" +
								"\t-O sort\t\t# sort method (insert,merge,quick)\r\n" +
								"\t-P primary\t# primary keys for indexing\r\n" +
								"\t-s field(s)\t# select fields\r\n" +
								"\t-S schema\t# the schema (key,value pairs)\r\n" +
								"\t-t storage\t# storage type (single,multi)\r\n" +
								"\t-V\t\t# vacuum (remove deleted items) from collection [not implemented]\r\n";
		
	// Main entry method
	public static void main( String args[] ) {
		// Check for command line argument are present
		if ( 0 == args.length ) {
			System.err.print( usage );
			System.exit( 1 );
		}
		
		String  cOption = "1";		// cache size
		int     cacheSize = 1;		// default cache size is 1Kb
		String  COption = "tmp";	// collection name: default to 'tmp' if none specified
		String  dOption = "binary";	// data store: default is binary
		String  fOption = null;		// filter (where)
		String  FOption = "csv";	// input format: default to CSV
		String  iOption = null;		// insert
		String  IOption = "linked";	// index type: default to linked list
		boolean nOption = false;	// no header in input file (csv, psv, tsv)
		String  oOption = null;		// order by	
		String  OOption = "insert";	// sort method: default to insert(ion)
		String  POption = null; 	//"STB,TITLE,DATE";	// Primary keys
		String  sOption = null;		// select
		String  SOption = null;		// schema
		String  tOption = "single";	// storage type: default to 'single'
		boolean VOption = false;	// vacuum
		
		DataStore dataStore = null;		// the data store type for this operation
		
		// Parse the command line arguments
		char opt;
		while ( ( opt = GetOpt( args, "c:C:d:f:F:i:I:no:O:P:s:S:t:V" ) ) != (char)-1 ) {
			switch ( opt ) {
			case 'c': cOption = optarg; break;
			case 'C': COption = optarg; break;
			case 'd': dOption = optarg; break;
			case 'f': fOption = optarg; break;
			case 'F': FOption = optarg; break;
			case 'i': iOption = optarg; break;
			case 'I': IOption = optarg; break;
			case 'n': nOption = true;   break;
			case 'o': oOption = optarg; break;
			case 'O': OOption = optarg; break;
			case 'P': POption = optarg; break;
			case 's': sOption = optarg; break;
			case 'S': SOption = optarg; break;
			case 't': tOption = optarg; break;
			case 'V': VOption = true;   break;
			}
		}
		
		if ( getOptIndex != args.length ) {
			System.err.println( "Invalid argument: " + args[ getOptIndex ] );
			System.err.println( usage );
			System.exit( 1 );
		}
		
		// Check for valid cache size option
		try {
			cacheSize = Integer.parseInt( cOption );
		}
		catch ( NumberFormatException e ) {
			System.err.println( "Invalid argument for cache size (-c): " + cOption );
			System.err.print( usage );
			System.exit( 1 );
		}
		
		// Check for valid data store option
		switch ( dOption ) {
			case "binary"	: dataStore = new BinaryStore( COption );
							  break;
			case "psv"		: dataStore = new PSVStore( COption );
							  break;
			case "csv"		: dataStore = new CSVStore( COption );
							  break;
			case "json"		: dataStore = new JSONStore( COption );
							  break;
			default			: System.err.println( "Invalid argument for data store (-d): " + dOption );
							  System.err.print( usage );
							  System.exit( 1 );
							  break;
		}
		
		// Check for valid storage type options
		try
		{
			dataStore.Storage( tOption );
		}
		catch ( StorageException e ) {
			System.err.println( e.getMessage() );
			System.err.print( usage );
			System.exit( 1 );
		}
		
		// Load & Verify Schema
		if ( null != SOption ) {
			String[] pairs = SOption.split( "," );	// pair key/values are comma separated
			if ( 0 == pairs.length ) {
				System.err.println( "Empty schema -S option : " + SOption );
				System.err.print( usage );
				System.exit( 1 );
			}
			
			ArrayList<Pair<String,String>> keys = new ArrayList<Pair<String,String>>( pairs.length ); 	// allocate space
			for ( int i = 0; i < pairs.length; i++ ) {			// process each key/value pair
				String[] keyval = pairs[ i ].split( ":" );
				if ( 2 != keyval.length ) {
					System.err.println( "Schema: key missing data type (-S): " + pairs[ i ] );
					System.err.print( usage );
					System.exit( 1 );
				}
				keys.add( new Pair( keyval[ 0 ].toLowerCase(), keyval[ 1 ].toLowerCase() ) );
			}
			
			// Load the schema
			try{
				dataStore.Schema( keys );
			}
			catch ( IllegalArgumentException e ) {
				System.err.println( e.getMessage() );
				System.err.print( usage );
				System.exit( 1 );
			}
		}
		
		// Load the Primary Keys for the Schema
		String[] primary = null;
		if ( null != POption ) {
			primary = POption.split( "," );	// primary key list separated by commas
			
			if ( 0 == primary.length ) {
				System.err.println( "Malformed argument for primary key (-P): " + POption );
				System.err.print( usage );
				System.exit( 1 );
			}
			
			// Load & Verify the Primary Keys
			try {
				dataStore.Primary( primary );
			}
			catch ( IllegalArgumentException e ) {
				System.err.println( e.getMessage() );
				System.err.print( usage );
				System.exit( 1 );
			}
		}
		
		// Check for valid index type options
		try
		{
			dataStore.Index( IOption );
		}
		catch ( StorageException e ) {
			System.err.println( e.getMessage() );
			System.err.print( usage );
			System.exit( 1 );
		}

		// Open the storage
		try {
			dataStore.Open();
		}
		catch ( StorageException e ) {
			System.err.println( e.getMessage() );
			System.exit( 1 );
		}
		
		// Insert
		if ( null != iOption ) {
			// Allocate the input file parser
			Parse parse = null;
			switch ( FOption ) {
			case "psv": parse = new PSVParse( iOption, dataStore, nOption ); break;
			case "csv": parse = new CSVParse( iOption, dataStore, nOption ); break;
			case "tsv": parse = new TSVParse( iOption, dataStore, nOption ); break;
			default	  : System.err.println( "Invalid argument for input format (-F): " + FOption );
						System.err.println( usage );
						System.exit( 1 );
						break;
			}
			
			try {
				parse.Open();
			}
			catch ( IOException e ) {
				System.err.println( e.getMessage() );
				System.err.println( usage );
				System.exit( 1 );
			}
			
			// parse and insert rows into data store
			try
			{
				parse.Parse();
			}
			catch ( StorageException e ) {
				System.err.println( e.getMessage() );
				System.exit( 1 );
			}
			catch ( IllegalArgumentException e ) {
				System.err.println( e.getMessage() );
				System.exit( 1 );
			}
			
			try {
				parse.Close();
			}
			catch ( IOException e ) {
				System.err.println( e.getMessage() );
				System.exit( 1 );
			}
		}
		
		// filter (where)
		Where where = null;
		if ( null != fOption ) {
			String[] filters = fOption.split( "," );
			ArrayList<Pair<String,String>> fkeys = new ArrayList<Pair<String,String>>( filters.length );
			
			// Check the syntax of the filter arguments
			for ( String filter : filters ) {
				String[] pair = filter.split( "=" );
				if ( pair.length != 2 ) {
					System.err.println( "Malformed argument for filter (-f): " + filter );
					System.err.println( usage );
					System.exit( 1 );
				}
		
				fkeys.add( new Pair<String,String>( pair[ 0 ], pair[ 1 ] ) );
				
				// TODO: only doing one where
				where = new Where(); where.op = Where.WhereOp.EQ; where.key = pair[ 0 ]; where.value = pair[ 1 ];
			}
			
			try {
				dataStore.checkKeyValArgs( fkeys );
			}
			catch ( IllegalArgumentException e ) {
				System.err.println( e.getMessage() );
				System.err.println( usage );
				System.exit( 1 );
			}
		}
		
		ArrayList<Data[]> result = null;	// query results
		
		// select
		String[] sKeys = null;
		if ( null != sOption ) {
			sKeys = sOption.split( "," );	// get keys (column names) from option arguments
			for ( int j = 0; j < sKeys.length; j++ )
				sKeys[ j ] = sKeys[ j ].toLowerCase();
			
			try {
				result = dataStore.Select( sKeys, where );
			}
			catch ( StorageException e ) {
				System.err.println( "Cannot select from datastore" );
				System.exit( 1 );
			}
			catch ( IllegalArgumentException e ) {
				System.err.println( e.getMessage() );
				System.err.print( usage );
				System.exit( 1 );
			}
		}
		
		// Display the results
		if ( null != result ) {
			// order by
			if ( null != oOption ) {
				Sort sort = null;
				switch ( OOption ) {
				case "insert" : sort = new InsertionSort( result, dataStore ); 
								break;
				case "quick"  : sort = new QuickSort( result, dataStore ); 
								break;
				default		  : System.err.println( "Invalid argument for sort type (-O): " + OOption );
								System.err.println( usage );
								System.exit( 1 );
								break;
				}
				
				 
				try
				{
					String[] orderby = oOption.split( "," );
					for ( int j = 0; j < orderby.length; j++ )
						orderby[ j ] = orderby[ j ].toLowerCase();
					
					dataStore.checkKeyArgs( orderby );
					
					result = sort.Sort( orderby );
				}
				catch ( IllegalArgumentException e ) {
					System.err.println( e.getMessage() );
					System.err.println( usage );
					System.exit( 1 );
				}
			}
	
			// Display each row in the result
			for ( Data[] row : result ) {
				// Display each column in the field
				for ( int j = 0; j < row.length; j++ ) {
					if ( row[ j ] != null )
						System.out.print( row[ j ].AsString() );
					if ( j < row.length - 1 )
						System.out.print( "," );
				}
				System.out.println( "" );
			}
		}
		
		// Vacuum
		if ( true == VOption ) {
			// TODO: do vacuum function
		}
		
		// Close the storage
		try {
			dataStore.Close();
		}
		catch ( StorageException e ) {
			System.err.println( e.getMessage() );
			System.exit( 1 );
		}
		
		System.exit( 0 );
	}
	
	// Getopt() command line parser
	static String optarg = null;	// argument for option
	static int getOptIndex = 0;		// current index in argument list
	
	// 	argv	: argument list
	//	parse	: option/arg to parse
	static private char GetOpt( String[] argv, String parse ) {
		// End of Argument List
		if ( getOptIndex >= argv.length ) {
			return (char) -1;
		}
		
		// Next argument is not an option
		if ( argv[ getOptIndex ].charAt( 0 ) != '-' ) {
			return (char) -1;
		}
		
		// Stray comma, no option follows
		if ( argv[ getOptIndex ].length() < 2 ) {
			System.err.println( "Invalid argument, no option letter follows comma" );
			System.err.print( usage );
			System.exit( 1 );
			return (char) -1;
		}
		
		// Not a valid option character after comma
		int opt = parse.indexOf( String.valueOf( argv[ getOptIndex ].charAt( 1 ) ) );
		if ( -1 == opt ) {
			System.err.println( "Invalid option: -" +  argv[ getOptIndex ].charAt( 1 ) );
			System.err.print( usage );
			System.exit( 1 );
			return (char)-1;
		}
		
		if ( parse.length() > opt + 1 ) {
			// option requires an argument
			if ( parse.charAt( opt + 1 ) == ':' ) {
				// argument follows option w/o space
				if ( argv[ getOptIndex ].length() > 2 ) {
					optarg = argv[ getOptIndex ].substring( 2 );
					return argv[ getOptIndex++ ].charAt( 1 );
				}
				// argument follows option with space
				else if ( getOptIndex + 1 < argv.length &&
				          argv[ getOptIndex + 1 ].charAt( 0 ) != '-' ) {
					getOptIndex += 2;
					optarg = argv[ getOptIndex - 1 ];
					return argv[ getOptIndex - 2 ].charAt( 1 );	  
				}
				// No argument follows option
				System.err.println( "Missing argument for -" + argv[ getOptIndex ].charAt( 1 ) + " option" );
				System.err.print( usage );
				System.exit( 1 );
				return (char) -1;
			}
		}
		
		// current option parsed
		return argv[ getOptIndex++ ].charAt( 1 );	
	}
}