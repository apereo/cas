```properties
# Enable the backing map to be cacheable
# cas.ticket.registry.in-memory.cache=true

# cas.ticket.registry.in-memory.load-factor=1
# cas.ticket.registry.in-memory.concurrency=20
# cas.ticket.registry.in-memory.initial-capacity=1000
```

{% include {{ version }}/signing-encryption.md configKey="cas.ticket.registry.in-memory" signingKeySize="512" encryptionKeySize="16" encryptionAlg="AES" %}
