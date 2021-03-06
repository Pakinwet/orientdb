package com.orientechnologies.orient.server.distributed.impl.coordinator.transaction;

import com.orientechnologies.orient.client.remote.message.tx.ORecordOperationRequest;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.serialization.serializer.record.binary.ORecordSerializerNetworkV37;
import com.orientechnologies.orient.server.distributed.impl.coordinator.ODistributedCoordinator;
import com.orientechnologies.orient.server.distributed.impl.coordinator.ODistributedMember;
import com.orientechnologies.orient.server.distributed.impl.coordinator.OSubmitRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OTransactionSubmit implements OSubmitRequest {
  private       OSessionOperationId           operationId;
  private final List<ORecordOperationRequest> operations;

  public OTransactionSubmit(OSessionOperationId operationId, Collection<ORecordOperation> ops) {
    this.operationId = operationId;
    this.operations = genOps(ops);
  }

  public static List<ORecordOperationRequest> genOps(Collection<ORecordOperation> ops) {
    List<ORecordOperationRequest> operations = new ArrayList<>();
    for (ORecordOperation txEntry : ops) {
      if (txEntry.type == ORecordOperation.LOADED)
        continue;
      ORecordOperationRequest request = new ORecordOperationRequest();
      request.setType(txEntry.type);
      request.setVersion(txEntry.getRecord().getVersion());
      request.setId(txEntry.getRecord().getIdentity());
      request.setRecordType(ORecordInternal.getRecordType(txEntry.getRecord()));
      switch (txEntry.type) {
      case ORecordOperation.CREATED:
      case ORecordOperation.UPDATED:
        request.setRecord(ORecordSerializerNetworkV37.INSTANCE.toStream(txEntry.getRecord(), false));
        request.setContentChanged(ORecordInternal.isContentChanged(txEntry.getRecord()));
        break;
      case ORecordOperation.DELETED:
        break;
      }
      operations.add(request);
    }
    return operations;
  }

  @Override
  public void begin(ODistributedMember member, ODistributedCoordinator coordinator) {
    OTransactionFirstPhaseResponseHandler responseHandler = new OTransactionFirstPhaseResponseHandler(operationId, this, member);
    coordinator.sendOperation(this, new OTransactionFirstPhaseOperation(this.operationId, this.operations), responseHandler);
  }
}
