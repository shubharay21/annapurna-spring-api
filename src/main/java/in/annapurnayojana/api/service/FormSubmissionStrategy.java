package in.annapurnayojana.api.service;

import in.annapurnayojana.api.dto.FormSubmissionPayload;

public interface FormSubmissionStrategy {
    void submit(FormSubmissionPayload payload);
}
