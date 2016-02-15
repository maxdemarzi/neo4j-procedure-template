package example;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.helpers.collection.MapUtil;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.bolt.BoltKernelExtension.Settings.connector;
import static org.neo4j.bolt.BoltKernelExtension.Settings.enabled;

public class LegacyFullTextIndexTest
{
    // This rule starts a Neo4j instance for us
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()

            // This is the Procedure we want to test
            .withProcedure( FullTextIndex.class )

            // Temporary until Neo4jRule includes Bolt by default
            .withConfig( connector( 0, enabled ), "true" );

    @Test
    public void shouldAllowIndexingAndFindingANode() throws Throwable
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( "bolt://localhost" ) )
        {

            // Given I've started Neo4j with the FullTextIndex procedure class
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // And given I have a node in the database
            long nodeId = (long) neo4j.getGraphDatabaseService().execute("CREATE (p:User {name:'Brookreson'}) RETURN id(p)")
                    .next().get("id(p)");

            // When I use the index procedure to index a node
            neo4j.getGraphDatabaseService().execute( "CALL example.index({id}, ['name'])", MapUtil.map("id", nodeId ) );

            // Then I can search for that node with lucene query syntax
            Result result = neo4j.getGraphDatabaseService().execute( "CALL example.search('User', 'name:Brook*')" );
            assertThat( (long)result.next().get( "nodeId" ), equalTo( nodeId ) );
        }
    }
}
