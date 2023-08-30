/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

use crate::canary::CanaryError;
use crate::mk_canary;
use aws_config::SdkConfig;
use aws_sdk_transcribestreaming as transcribe;
use bytes::BufMut;
use transcribe::primitives::Blob;
use transcribe::types::{
    AudioEvent, AudioStream, LanguageCode, MediaEncoding, TranscriptResultStream,
};

const CHUNK_SIZE: usize = 8192;
use crate::canary::CanaryEnv;

mk_canary!(
    "transcribe_canary",
    |sdk_config: &SdkConfig, env: &CanaryEnv| {
        transcribe_canary(
            transcribe::Client::new(sdk_config),
            env.expected_transcribe_result.clone(),
        )
    }
);

pub async fn transcribe_canary(
    client: transcribe::Client,
    expected_transcribe_result: String,
) -> anyhow::Result<()> {
    let input_stream = transcribe::primitives::FnStream::new(|tx| {
        Box::pin(async move {
            let pcm = pcm_data();
            for chunk in pcm.chunks(CHUNK_SIZE) {
                tx.send(Ok(AudioStream::AudioEvent(
                    AudioEvent::builder().audio_chunk(Blob::new(chunk)).build(),
                )))
                .await
                .expect("send should succeed");
            }
        })
    });

    let mut output = client
        .start_stream_transcription()
        .language_code(LanguageCode::EnGb)
        .media_sample_rate_hertz(8000)
        .media_encoding(MediaEncoding::Pcm)
        .audio_stream(input_stream.into())
        .send()
        .await?;

    let mut full_message = String::new();
    while let Some(event) = output.transcript_result_stream.recv().await? {
        match event {
            TranscriptResultStream::TranscriptEvent(transcript_event) => {
                let transcript = transcript_event.transcript.unwrap();
                for result in transcript.results.unwrap_or_default() {
                    if !result.is_partial {
                        let first_alternative = &result.alternatives.as_ref().unwrap()[0];
                        full_message += first_alternative.transcript.as_ref().unwrap();
                        full_message.push(' ');
                    }
                }
            }
            otherwise => panic!("received unexpected event type: {:?}", otherwise),
        }
    }

    if expected_transcribe_result != full_message.trim() {
        Err(CanaryError(format!(
            "Transcription from Transcribe doesn't look right:\n\
            Expected: `{}`\n\
            Actual:   `{}`\n",
            expected_transcribe_result,
            full_message.trim()
        ))
        .into())
    } else {
        Ok(())
    }
}

fn pcm_data() -> Vec<u8> {
    let reader =
        hound::WavReader::new(&include_bytes!("../../audio/hello-transcribe-8000.wav")[..])
            .expect("valid wav data");
    let samples_result: hound::Result<Vec<i16>> = reader.into_samples::<i16>().collect();

    let mut pcm: Vec<u8> = Vec::new();
    for sample in samples_result.unwrap() {
        pcm.put_i16_le(sample);
    }
    pcm
}
