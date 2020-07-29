package com.goldberg.keypairgeneratorspeedtest;

import java.security.KeyPair;

final class KeypairGenerationResult
{
    public KeypairGenerationResult(KeyPair keyPair, long generationTimeMs)
    {
        this.keyPair = keyPair;
        this.generationTimeMs = generationTimeMs;
    }

    KeyPair keyPair;
    long generationTimeMs;
}
