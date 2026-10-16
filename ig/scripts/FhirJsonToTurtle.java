// Usage: java -cp validator_cli.jar FhirJsonToTurtle.java <fhirVersion> <in.json>... 
// Writes <in>.ttl next to each input.
import java.nio.file.Files;
import java.nio.file.Path;

import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.ValidationEngine;

public class FhirJsonToTurtle {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("usage: java -cp validator_cli.jar FhirJsonToTurtle.java <fhirVersion> <in.json>...");
      System.exit(2);
    }
    String version = args[0];
    String core = VersionUtilities.packageForVersion(version) + "#" + VersionUtilities.getCurrentVersion(version);
    ValidationEngine engine = new ValidationEngine.ValidationEngineBuilder()
        .withVersion(version)
        .withNoTerminologyServer()
        .fromSource(core);
    for (int i = 1; i < args.length; i++) {
      Path in = Path.of(args[i]);
      Path out = in.resolveSibling(in.getFileName().toString().replaceFirst("\\.json$", "") + ".ttl");
      Files.write(out, engine.convertFormat(Files.readAllBytes(in), FhirFormat.JSON, FhirFormat.TURTLE));
      System.out.println(in + " -> " + out);
    }
  }
}
