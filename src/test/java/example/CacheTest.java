package example;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.helpers.collection.MapUtil;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.bolt.BoltKernelExtension.Settings.connector;
import static org.neo4j.bolt.BoltKernelExtension.Settings.enabled;

public class CacheTest {

    // This rule starts a Neo4j instance for us
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()

            // This is the Procedure we want to test
            .withProcedure( Cache.class )
            .withFixture("CREATE (n:User {username:'maxdemarzi'})")

            // Temporary until Neo4jRule includes Bolt by default
            .withConfig( connector( 0, enabled ), "true" );

    @Test
    public void shoulFindANodeViaCache() throws Throwable
    {
        // When I use the index procedure to index a node
        Result result = neo4j.getGraphDatabaseService().execute(
                "CALL max.cache({label},{property},{id})",
                MapUtil.map("label", "User",
                        "property", "username",
                        "id", "maxdemarzi") );

        assertThat( ((Node)result.next().get( "node" )).getProperty("username"), equalTo( "maxdemarzi" ) );
        result.close();
        result = neo4j.getGraphDatabaseService().execute(
                "CALL max.cache({label},{property},{id})",
                MapUtil.map("label", "User",
                        "property", "username",
                        "id", "maxdemarzi") );
        assertThat( ((Node)result.next().get( "node" )).getProperty("username"), equalTo( "maxdemarzi" ) );
        result.close();
    }
}
