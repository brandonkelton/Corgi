/*
  store func names with func
*/

import java.util.ArrayList;

public class FuncTable {

  private ArrayList<String> names;
  private ArrayList<Node> nodes;

  public FuncTable() {
    names = new ArrayList<String>();
    nodes = new ArrayList<Node>();
  }

  public String toString() {
    String s = "----\n";
    for( int k=0; k<names.size(); k++ ) {
      s += names.get(k) + " " + nodes.get(k) + "\n";
    }
    return s;
  }

  public int size() {
    return names.size();
  }

  // store node for name, adding name if not already
  // there
  public void store( String name, Node node ) {

     int loc = findName( name );
   
     if ( loc < 0 ) {// add new pair
        names.add( name );
        nodes.add( node );
     }
     else {// change node for existing pair
        nodes.set( loc, node );
     } 

  }// store

  // retrieve node for given name
  public Node retrieve( String name ) {

     int loc = findName( name );

     if ( loc >= 0 ) {// add new pair
        return nodes.get( loc );
     }
     else {
        System.out.println("variable [" + name + "] not found");
        System.exit(1);
        return null;
     }
  
  }// retrieve

  // return index of name in names, or -1 if
  // not found
  private int findName( String name ) {
     // locate name
     int loc = -1;
     for (int k=0; k<names.size() && loc<0; k++) {
        if ( names.get(k).equals(name) ) {
           loc = k;
        }
     }

     return loc;
  }// findName

}