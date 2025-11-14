package tickets;

import java.io.IOException;
import java.io.OutputStream;

public interface ExportStrategy {
    void export(Ticket ticket, OutputStream out) throws IOException;
}
