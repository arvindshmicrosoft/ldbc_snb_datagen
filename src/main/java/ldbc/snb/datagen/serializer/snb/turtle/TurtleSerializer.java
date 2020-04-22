package ldbc.snb.datagen.serializer.snb.turtle;

import ldbc.snb.datagen.DatagenParams;
import ldbc.snb.datagen.hadoop.HadoopConfiguration;
import ldbc.snb.datagen.hadoop.writer.HdfsWriter;
import ldbc.snb.datagen.serializer.Serializer;
import ldbc.snb.datagen.serializer.snb.csv.FileName;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TurtleSerializer extends Serializer<HdfsWriter> {

    default Map<FileName, HdfsWriter> initialize(Configuration conf, int reducerId, boolean dynamic, List<FileName> fileNames) throws IOException {
        Map<FileName, HdfsWriter> writers = new HashMap<>();

        for (FileName f : fileNames) {
            HdfsWriter w = new HdfsWriter(
                    conf.get("ldbc.snb.datagen.serializer.socialNetworkDir"),
                    f.toString() + "_" + reducerId,
                    DatagenParams.numUpdateStreams,
                    HadoopConfiguration.isCompressed(conf), "ttl");
            writers.put(f, w);

            w.writeAllPartitions(Turtle.getNamespaces());
            w.writeAllPartitions(Turtle.getStaticNamespaces());
        }
        return writers;
    }

}
