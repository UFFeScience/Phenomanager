package com.uff.model.invoker.service.provider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.NodeType;
import com.uff.model.invoker.domain.VirtualMachineConfig;

@Component
public class CloudProviderService {
	
	private static final Logger log = LoggerFactory.getLogger(CloudProviderService.class);

	private static final String IP_RANGE_PERMISSION = "0.0.0.0/0";
	private static final String GROUP_NAME_LABEL = "group-name";
	private static final String KEY_NAME_LABEL = "key-name";
	private static final String TAG_FILTER_LABEL = "tag:";
	private static final String CLUSTER_HEADER_PREFIX = "PM-";
	private static final String RUNNING_STATE = "running";
	private static final String NODE_TYPE_LABEL = "NodeType";
	private static final String INSTANCE_STATE_NAME_LABEL = "instance-state-name";
	private static final String CLUSTER_LABEL_NAME = "Name";
	
	public AmazonEC2Client authenticateProvider(ExecutionEnvironment executionEnvironment) {
		AWSCredentials credentials = new BasicAWSCredentials(executionEnvironment.getAccessKey(), 
				executionEnvironment.getSecretKey());
	    
		return new AmazonEC2Client(credentials);
	}
	
	public AmazonMachine getControlInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult controls = getDescribeControlFromCluster(amazonClient, clusterName);
     
        for (Reservation reservation : controls.getReservations()) {
        	for (Instance instance : reservation.getInstances()) {
        		return AmazonMachine.builder()
            			.publicDNS(instance.getPublicDnsName())
            			.publicIP(instance.getPublicIpAddress())
            			.privateIP(instance.getPrivateIpAddress()).build();
            }
        }
        
        return null;
    }
	
	public void createCluster(AmazonEC2Client amazonClient, ExecutionEnvironment executionEnvironment, String absolutePath) throws Exception {
        Boolean hasAliveInstance = hasAliveInstanceFromCluster(amazonClient, executionEnvironment.getClusterName());
        Boolean hasSecurityGroup = hasAliveSecurityGroupFromCluster(amazonClient, executionEnvironment.getClusterName());
        Boolean hasKeyPairGroup = hasAliveKeyPairFromCluster(amazonClient, executionEnvironment.getClusterName());

        if (!hasAliveInstance && !hasSecurityGroup && !hasKeyPairGroup) {
        	executionEnvironment.getVirtualMachines().stream().findFirst().get().setAmountInstantiated(1);
            
            log.info("PhenoManager is communicating with Amazon");

            createSecurityGroup(amazonClient, executionEnvironment.getClusterName());
            createKeyPair(amazonClient, executionEnvironment.getClusterName());

            log.info("Creating pool of virtual machines");

            List<AmazonMachine> machines = startVirtualMachines(new ArrayList<>(executionEnvironment.getVirtualMachines()), 
            		executionEnvironment.getClusterName(), 
            		executionEnvironment.getImage(), 
            		amazonClient);

            for (AmazonMachine mac : machines) {
            	log.info("Machine mac address: [{}]", mac.toString());
            }
            log.info("Amount of Virtual machines: [{}] ", machines.size());
        
        } else {
        	log.warn("There is an alive instance with this cluster name!");
        }
    }
	
	private String createSecurityGroup(AmazonEC2Client amazon, String clusterName) {
        String newGroupName = getSecurityGroupName(clusterName);
        CreateSecurityGroupRequest r1 = new CreateSecurityGroupRequest(newGroupName, "PhenoManager temporal group");
        amazon.createSecurityGroup(r1);
        AuthorizeSecurityGroupIngressRequest r2 = new AuthorizeSecurityGroupIngressRequest();
        r2.setGroupName(newGroupName);

        log.info("Creating http message rules");
        IpPermission permission = new IpPermission();
        permission.setIpProtocol("tcp");
        permission.setFromPort(80);
        permission.setToPort(80);
        List<String> ipRanges = new ArrayList<String>();
        ipRanges.add(IP_RANGE_PERMISSION);
        permission.setIpRanges(ipRanges);

        log.info("Creating ssh message rules");
        IpPermission permission1 = new IpPermission();
        permission1.setIpProtocol("tcp");
        permission1.setFromPort(22);
        permission1.setToPort(22);
        List<String> ipRanges1 = new ArrayList<String>();
        ipRanges1.add(IP_RANGE_PERMISSION);
        permission1.setIpRanges(ipRanges1);

        log.info("Creating https message rules");
        IpPermission permission2 = new IpPermission();
        permission2.setIpProtocol("tcp");
        permission2.setFromPort(443);
        permission2.setToPort(443);
        List<String> ipRanges2 = new ArrayList<String>();
        ipRanges2.add(IP_RANGE_PERMISSION);
        permission2.setIpRanges(ipRanges2);

        log.info("Creating tcp message rules");
        IpPermission permission3 = new IpPermission();
        permission3.setIpProtocol("tcp");
        permission3.setFromPort(0);
        permission3.setToPort(65535);
        List<String> ipRanges3 = new ArrayList<String>();
        ipRanges3.add(IP_RANGE_PERMISSION);
        permission3.setIpRanges(ipRanges3);

        log.info("Adding all permission rules");
        List<IpPermission> permissions = new ArrayList<IpPermission>();
        permissions.add(permission);
        permissions.add(permission1);
        permissions.add(permission2);
        permissions.add(permission3);
        r2.setIpPermissions(permissions);

        amazon.authorizeSecurityGroupIngress(r2);
        return newGroupName;
    }
	
	private Boolean hasAliveInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
		DescribeInstancesResult result = getAliveInstancesFromCluster(amazonClient, clusterName);
        
		for (Reservation reservation : result.getReservations()) {
            Integer instances = reservation.getInstances().size();
            
            if (instances > 0) {
                return Boolean.TRUE;
            }
        }
		
        return Boolean.FALSE;
    }
	
	private DescribeInstancesResult getAliveInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName) {
		List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList(RUNNING_STATE));
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, INSTANCE_STATE_NAME_LABEL, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(CLUSTER_LABEL_NAME, getVirtualMachinesName(clusterName)));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        return amazonClient.describeInstances(iRequest);
    }
	
	private DescribeInstancesRequest getDescribeInstancesRequest(ArrayList<Tag> tags, List<Filter> filters) {
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        
        for (Tag tag : tags) {
            Filter filter = getFilterFromTag(tag.getKey(), tag.getValue());
            filters.add(filter);
        }
        request.setFilters(filters);
        
        return request;
	}
	
	private Filter getFilterFromTag(String tag, String value) {
        Filter filter = new Filter();
        filter.setName(TAG_FILTER_LABEL + tag);
        filter.setValues(Collections.singletonList(value));
        return filter;
	}
	
	private String getVirtualMachinesName(String clusterName) {
		String clusterHeaderName = CLUSTER_HEADER_PREFIX;
        return clusterHeaderName + clusterName;
    }
	
	private List<Filter> createFilter(List<Filter> filters, String key, List<String> values) {
        Filter newFilter = new Filter(key);
        newFilter.setValues(values);
        filters.add(newFilter);
        return filters;
    }
	
	private Boolean hasAliveSecurityGroupFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeSecurityGroupsResult result = getAliveSecurityGroupFromACluster(amazonClient, clusterName);
        return (result.getSecurityGroups().size() > 0);
    }
	
	private DescribeSecurityGroupsResult getAliveSecurityGroupFromACluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeSecurityGroupsRequest iRequest = new DescribeSecurityGroupsRequest();
        List<Filter> filter = new ArrayList<Filter>();

        List<String> values = new ArrayList<String>();
        values.add(getSecurityGroupName(clusterName));
        createFilter(filter, GROUP_NAME_LABEL, values);
        iRequest.setFilters(filter);
        
        return amazonClient.describeSecurityGroups(iRequest);
    }
	
	private String getSecurityGroupName(String clusterName) {
		String securityGroupHeaderName = CLUSTER_HEADER_PREFIX + "SG-";
        return securityGroupHeaderName + clusterName;
    }
	
	private Boolean hasAliveKeyPairFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeKeyPairsResult result = getAliveKeyPairFromACluster(amazonClient, clusterName);
        return (result.getKeyPairs().size() > 0);
    }
	
	private DescribeKeyPairsResult getAliveKeyPairFromACluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeKeyPairsRequest iRequest = new DescribeKeyPairsRequest();
        List<Filter> filter = new ArrayList<Filter>();

        List<String> values = new ArrayList<String>();
        values.add(getKeyPairName(clusterName));
        createFilter(filter, KEY_NAME_LABEL, values);
        iRequest.setFilters(filter);
        
        return amazonClient.describeKeyPairs(iRequest);
    }
	
	private String getKeyPairName(String clusterName) {
		String keyHeaderName = CLUSTER_HEADER_PREFIX + "Key-";
        return keyHeaderName + clusterName;
    }
	
	private List<AmazonMachine> startVirtualMachines(List<VirtualMachineConfig> virtualMachineConfigs, 
            String clusterName, String image, AmazonEC2Client amazonClient) throws InterruptedException {
        
		List<AmazonMachine> amazonMachines = new ArrayList<AmazonMachine>();

		DescribeInstancesResult describeInstancesRequest;
        StartInstancesRequest startInstanceRequest;

        Set<Instance> instances = new HashSet<>();
        List<String> instanceIds = new LinkedList<String>();
        List<String> resources = new LinkedList<String>();
        List<Reservation> reservations;
        
        Integer rank = 0;
        Integer i, k = 0;
        Integer controlType = 0;
        VirtualMachineConfig instanceResults;

        List<RunInstancesResult> resultInstances = createMachineInstanceRequest(
        		virtualMachineConfigs, clusterName, image, amazonClient);

        log.info("PhenoManager is waiting virtual machines to start...");
        sleep(30000);
        log.info("All virtual machines are instantiated");

        Boolean hasControlInstances = hasControlInstanceFromCluster(amazonClient, clusterName);
        Boolean hasCoreInstances = hasCoreInstanceFromCluster(amazonClient, clusterName);
        Boolean first = true;
        Tag nameTag = new Tag(CLUSTER_LABEL_NAME, getVirtualMachinesName(clusterName));
        
        for (RunInstancesResult runInstanceResult : resultInstances) {
            first = createTags(amazonClient, nameTag, runInstanceResult, resources, instanceIds, hasControlInstances,
					hasCoreInstances, first);

            String reservationId = runInstanceResult.getReservation().getReservationId();
            describeInstancesRequest = amazonClient.describeInstances();
            reservations = describeInstancesRequest.getReservations();
            
            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
               
                if (reservation.getReservationId().equals(reservationId)) {
                    k = reservation.getInstances().size();
                    instanceResults = virtualMachineConfigs.get(controlType);
                    
                    while (instanceResults.getAmountInstantiated() < 1) {
                        controlType++;
                        instanceResults = virtualMachineConfigs.get(controlType);
                    }
                    
                    for (i = 0; i < k; i++) {
                        amazonMachines.add(AmazonMachine.builder()
                        		.rank(rank)
                        		.publicDNS(reservation.getInstances().get(i).getPublicDnsName())
                        		.publicIP(reservation.getInstances().get(0).getPublicIpAddress())
                        		.privateIP(reservation.getInstances().get(0).getPrivateIpAddress())
                        		.type(instanceResults.getType())
                        		.numberOfCores(instanceResults.getNumberOfCores())
                        		.build());
                        rank++;
                        controlType++;
                    }
                }
            }

            startInstanceRequest = new StartInstancesRequest(instanceIds);
            amazonClient.startInstances(startInstanceRequest);
        }
        
        return amazonMachines;
    }

	private Boolean createTags(AmazonEC2Client amazonClient, Tag nameTag, RunInstancesResult runInstanceResult,
			List<String> resources, List<String> instanceIds, Boolean hasControlInstances, Boolean hasCoreInstances,
			Boolean first) {
		
		CreateTagsRequest createTagsRequest;
		String createdInstanceId;
		List<String> currentResource;
		List<Tag> tags;
		Tag nodeTypeTag;
		
		List<Instance> resultInstance = runInstanceResult.getReservation().getInstances();
		
		for (Instance instance : resultInstance) {
		    currentResource = new LinkedList<String>();
		    createdInstanceId = instance.getInstanceId();
		    
		    log.info("New virtual machine has been created: [{}]", instance.getInstanceId());
		    
		    resources.add(createdInstanceId);
		    currentResource.add(createdInstanceId);
		    instanceIds.add(createdInstanceId);
		    
		    tags = new LinkedList<Tag>();
		    tags.add(nameTag);
		    
		    if (!hasControlInstances && first) {
		        nodeTypeTag = new Tag(NODE_TYPE_LABEL, NodeType.CONTROL.toString());
		        first = false;
		    
		    } else if (!hasCoreInstances && first) {
		        nodeTypeTag = new Tag(NODE_TYPE_LABEL, NodeType.SUPERNODE.toString());
		        first = false;
		    
		    } else {
		        nodeTypeTag = new Tag(NODE_TYPE_LABEL, NodeType.NODE.toString());
		    }
		    
		    tags.add(nodeTypeTag);
		    createTagsRequest = new CreateTagsRequest(currentResource, tags);
		    amazonClient.createTags(createTagsRequest);
		}
		
		return first;
	}

	private List<RunInstancesResult> createMachineInstanceRequest(List<VirtualMachineConfig> virtualMachineConfigs, String clusterName,
			String image, AmazonEC2Client amazonClient) {
		
		List<RunInstancesResult> instanceResults = new ArrayList<>();
		RunInstancesRequest runInstanceRequest;
        RunInstancesResult instanceResult = null;
        
        for (VirtualMachineConfig virtualMachine : virtualMachineConfigs) {
        	if (virtualMachine.getAmountInstantiated() > 0) {
                runInstanceRequest = new RunInstancesRequest(image, virtualMachine.getAmountInstantiated(), virtualMachine.getAmountInstantiated());
                runInstanceRequest.setInstanceType(virtualMachine.getType());
                runInstanceRequest.setKeyName(getKeyPairName(clusterName));
                runInstanceRequest.setSecurityGroups(getSecurityGroupList(clusterName));
                instanceResult = amazonClient.runInstances(runInstanceRequest);
                instanceResults.add(instanceResult);
            }
        }
        
        return instanceResults;
	}
	
	private Boolean hasCoreInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getDescribeSuperNodesFromCluster(amazonClient, clusterName);
        
        for (Reservation reservation : result.getReservations()) {
            Integer instances = reservation.getInstances().size();
            
            if (instances > 0) {
                return Boolean.TRUE;
            }
        }
        
        result = getDescribeNodesFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
        	Integer instances = reservation.getInstances().size();
            
        	if (instances > 0) {
                return Boolean.TRUE;
            }
        }
        
        return Boolean.FALSE;
    }
	
	private DescribeInstancesResult getDescribeNodesFromCluster(AmazonEC2Client amazonClient, String clusterName) {
		List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList(RUNNING_STATE));
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, INSTANCE_STATE_NAME_LABEL, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(CLUSTER_LABEL_NAME, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(NODE_TYPE_LABEL, NodeType.NODE.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
	
	private Boolean hasControlInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getDescribeControlFromCluster(amazonClient, clusterName);
        
        for (Reservation reservation : result.getReservations()) {
            Integer instances = reservation.getInstances().size();
        
            if (instances > 0) {
                return Boolean.TRUE;
            }
        }
        
        return Boolean.FALSE;
    }
	
	private DescribeInstancesResult getDescribeSuperNodesFromCluster(AmazonEC2Client amazonClient, String clusterName) {
		List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList(RUNNING_STATE));
        ArrayList<Tag> tags = new ArrayList<Tag>();
        
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, INSTANCE_STATE_NAME_LABEL, runningInstanceStates);
        
        tags.add(new Tag(CLUSTER_LABEL_NAME, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(NODE_TYPE_LABEL, NodeType.SUPERNODE.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
	
	private DescribeInstancesResult getDescribeControlFromCluster(AmazonEC2Client amazonClient, String clusterName) {
		List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList(RUNNING_STATE));
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, INSTANCE_STATE_NAME_LABEL, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(CLUSTER_LABEL_NAME, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(NODE_TYPE_LABEL, NodeType.CONTROL.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
	
	private List<String> getSecurityGroupList(String clusterName) {
		String securityGroupHeaderName = CLUSTER_HEADER_PREFIX + "SG-";
        List<String> securityGroups = new ArrayList<String>();
        securityGroups.add(securityGroupHeaderName + clusterName);
        return securityGroups;
    }
	
	private void sleep(Integer milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        	log.error("Error while calling sleep", e);
        }
    }

	private String createKeyPair(AmazonEC2Client amazon, String clusterName) throws IOException {
        String keyName = getKeyPairName(clusterName);
        
        CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
        newKeyRequest.setKeyName(keyName);
        CreateKeyPairResult keyresult = amazon.createKeyPair(newKeyRequest);

        KeyPair keyPair = keyresult.getKeyPair();
        log.info("The PhenoManager key created: [{}]" + keyPair.getKeyName());

        File distFile = new File(keyName + ".pem");
        BufferedReader bufferedReader = new BufferedReader(new StringReader(keyPair.getKeyMaterial()));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(distFile));
        
        char buf[] = new char[1024];
        int len;
       
        while ((len = bufferedReader.read(buf)) != -1) {
            bufferedWriter.write(buf, 0, len);
        }
        
        bufferedWriter.flush();
        bufferedReader.close();
        bufferedWriter.close();
        
        return keyName;
    }
	
}