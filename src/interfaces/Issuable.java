package interfaces;

/**
 * Contract for any catalog item that can be issued and returned.
 * Implemented by Book so the issuing subsystem can work with any
 * future item type (e.g. Magazine, DVD) without modification.
 */
public interface Issuable {
    boolean isAvailable();
    void markIssued();
    void markReturned();
}
