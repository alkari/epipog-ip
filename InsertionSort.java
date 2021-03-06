import java.util.ArrayList;

// Implementation for Sorting Results using Insertion Sort algorithm
public class InsertionSort extends Sort {
	// Constructor
	//	_result : result list to be sorted
	public InsertionSort( ArrayList<Data[]> result, DataStore dataStore ) {
		super( result, dataStore );
	}

	// Method to sort an result list
	//	keys : columns to sort
	public ArrayList<Data[]> Sort( String[] keys ) 
		throws IllegalArgumentException
	{
		dataStore.checkKeyArgs( keys );
		
		// Sort for each key
		for ( String key : keys ) {
			Data[] temp;	// holds the interchanged element row
			
			// Use first row (header) in result to get order of keys in result
			int ncol = -1;
			for ( int i = 0; i < result.get( 0 ).length; i++ ) {
				if ( ((String)result.get( 0 )[ i ].Get()).compareTo( key ) == 0 ) {
					ncol = i;
					break;
				}
			}
			
			// Check if key is in the result
			if ( -1 == ncol )
				throw new IllegalArgumentException( "Sort key not in result: " + key );
			
			int length = result.size();
			for (int i = 2; i < length; i++) {	// start at row 2 (1 is the heading)
				for(int j = i ; j > 1 ; j--){
					if ( result.get( j )[ ncol ].LT( result.get( j - 1 )[ ncol ] ) ) {
						temp = result.get( j );
						result.set( j, result.get( j - 1 ) );
						result.set( j - 1, temp );
					}
				}
			}
		}
		
		return result;
	}
}