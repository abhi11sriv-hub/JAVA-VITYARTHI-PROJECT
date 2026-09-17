package interfaces;

/**
 * Contract for services that can produce a human-readable report string.
 * Allows ReportGenerator to treat different report sources uniformly.
 */
public interface Reportable {
    String generateReport();
}
