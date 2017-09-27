package org.apereo.cas.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class JdbcEditableAttributeValueRepositoryTest {

    @Test
    public void testGenerateUpdateSql() {
        
        HashMap<String,String> t1 = new LinkedHashMap<>();
        t1.put( "name", "joe" );
        t1.put( "color", "blue" );
        t1.put( "nothing", null );
        t1.put( "foo", "bar" );
        t1.put(  "empty", "" );
        
        String s1 = "UPDATE someTable SET name=?,color=?,nothing=?,foo=?,empty=? WHERE username=?";
        ArrayList<String> a1 = new ArrayList<>();
        a1.add( "joe" );
        a1.add( "blue" );
        a1.add( null );
        a1.add( "bar" );
        a1.add( "" );
        a1.add( "dbUser" );
        
        Pair<String,List<String>> result = 
                JdbcEditableAttributeValueRepository.generateUpdateSql("someTable", "dbUser", t1 );
        
        assert( a1.equals( result.getRight() ) );
        assert( s1.equals(  result.getLeft()  ) );
        
        HashMap<String,String> t2 = new LinkedHashMap<>();
        t2.put( "name", "Joe" );
        
        String s2 = "UPDATE another_table SET name=? WHERE username=?";
        ArrayList<String> a2 = new ArrayList<>();
        a2.add( "Joe" );
        a2.add( "db_user" );
        
        
        result = JdbcEditableAttributeValueRepository.generateUpdateSql("another_table", "db_user", t1 );
        
        assert( a2.equals( result.getRight() ) );
        assert( s2.equals( result.getLeft() ) );
        
    }
}
