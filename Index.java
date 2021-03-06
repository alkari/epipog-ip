import javafx.util.Pair;
import java.util.ArrayList;

// Abstract Layer for Indexing
public abstract class Index {
	
	// constructor
	public Index( String collectionName, String[] keys ) {
		this.collectionName = collectionName;
		this.keys           = keys;
	}
	
	private String    collectionName;	// collection name
	private String[]  keys;				// keys being indexed
	
	// Get/Set Accessors for keys
	public  String[] Keys() { return keys; }
	public  void Keys( String[] keys ) { this.keys = keys; }
	
	// Method for adding a hashed entry to the index
	// Return:
	//	-1 : new entry (not found)
	//	not -1 : position of found entry
	public abstract long Add( long hash, long pos );
	
	// Method for finding a hashed entry from index
	// Return
	//	-1 : not found
	//	not -1 : found, return position in datastore
	public abstract long Find( long hash );
	
	// Method for removing a hash entry from the index
	// Return
	//	-1 : not found
	//	not -1 : found and removed
	protected abstract long Remove( long hash );
	
	// Method to return the position in storage of the nth record (row/document)
	// Return
	//	-1 : no such element
	//  >0 : storage position
	public abstract long Pos( int nth );
	
	// Method for getting in-memory index
	public abstract Object Index();
	
	// Method for setting in-memory index
	public abstract void Index( Object index );
}