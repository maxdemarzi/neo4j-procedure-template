package example;

import com.google.common.cache.CacheBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Cache {

    public static final com.google.common.cache.Cache<String, Long> cache = CacheBuilder.newBuilder()
            .maximumSize(1000000).build();

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure("max.cache")
    public Stream<Output> getByIndex(@Name("label") String label,
                            @Name("property") String property,
                            @Name("id") String id ) throws ExecutionException {


        Long nodeId = cache.get(id, () -> db.findNode(Label.label(label), property, id).getId() );
        return Stream.of( new Output(db.getNodeById(nodeId)));
    }

    static public class Output
    {
        public Node node;

        public Output(Node node )
        {
            this.node = node;

        }
    }
}
