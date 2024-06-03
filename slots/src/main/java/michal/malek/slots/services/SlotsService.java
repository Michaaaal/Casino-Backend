package michal.malek.slots.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import michal.malek.slots.exceptions.SlotGameNotFoundException;
import michal.malek.slots.models.Slot;
import michal.malek.slots.models.SlotsGameEntity;
import michal.malek.slots.models.SlotsGameEntityDTO;
import michal.malek.slots.repositories.SlotsGameRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotsService {
    private final int SLOTS_AMOUNT = 12;
    private final int MAX_SLOT_ID = 8;
    private final int MIN_SLOT_ID = 1;

    @Value("${accounting.service.url}")
    private String accountingUrl;

    private final SlotsGameRepository slotsGameRepository;
    private final HashMap< Integer, List<List<Integer>>> multiplierCombinations = new HashMap<>();
    {
        //multiplier 10
        List<Integer> list10_1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> list10_2 = Arrays.asList(5, 6, 7, 8);
        List<Integer> list10_3 = Arrays.asList(9, 10, 11, 12);
        List<List<Integer>> list10 = Arrays.asList(list10_1,list10_2,list10_3);

        //multiplier 5
        List<Integer> list5_1 = Arrays.asList(5, 2, 3, 8);
        List<Integer> list5_2 = Arrays.asList(5, 10, 11, 8);
        List<Integer> list5_3 = Arrays.asList(1, 6, 7, 4);
        List<Integer> list5_4 = Arrays.asList(9, 6, 7, 12);
        List<List<Integer>> list5= Arrays.asList(list5_1, list5_2, list5_3, list5_4);

        //multiplier 3
        List<Integer> list3_1 = Arrays.asList(1,2,3);
        List<Integer> list3_2 = Arrays.asList(2,3,4);
        List<Integer> list3_3 = Arrays.asList(5,6,7);
        List<Integer> list3_4 = Arrays.asList(6,7,8);
        List<Integer> list3_5 = Arrays.asList(9,10,11);
        List<Integer> list3_6 = Arrays.asList(10,11,12);
        List<Integer> list3_7 = Arrays.asList(1,6,11);
        List<Integer> list3_8 = Arrays.asList(2,7,12);
        List<Integer> list3_9 = Arrays.asList(9,6,3);
        List<Integer> list3_10 = Arrays.asList(10,7,4);
        List<List<Integer>> list3 = Arrays.asList(list3_1,list3_4,list3_6,list3_3,list3_5,list3_2,list3_7,list3_8,list3_9,list3_10);

        multiplierCombinations.put(10, list10);
        multiplierCombinations.put(5, list5);
        multiplierCombinations.put(3, list3);
    }

    public HashMap<Integer,Integer> getRandomSlotsMatrix(){
        Random random = new Random();
        HashMap<Integer, Integer> randomSlotsMatrix = new HashMap<>();

        for (int i=1; i<= SLOTS_AMOUNT; i++){
            int randomSlot = random.nextInt(1, 9);
            randomSlotsMatrix.put( i , randomSlot );
        }

        return randomSlotsMatrix;
    }

    public SlotsGameEntity spin(long stake, long userId){
        HashMap<Integer, Integer> slotsMatrix = this.getRandomSlotsMatrix();

        long winAmount = 0;
        int totalMultiplier = 1;
        Set<Integer> winningSlots = new HashSet<>();

        for (int i = MIN_SLOT_ID; i <= MAX_SLOT_ID ; i++){
            if(slotsMatrix.containsValue(i)){
                int slotId = i;
                List<Integer> activeSlots = slotsMatrix
                        .entrySet()
                        .stream()
                        .filter(elem -> elem.getValue() == slotId)
                        .map(Map.Entry::getKey).toList();

                if(activeSlots.isEmpty())
                    continue;

                for(var multiplierCombination : multiplierCombinations.entrySet()){
                    Integer multiplier = multiplierCombination.getKey();
                    List<List<Integer>> combinations = multiplierCombination.getValue();
                    for(var combination : combinations){
                        if(new HashSet<>(activeSlots).containsAll(combination)){
                            winAmount += (long) multiplier * stake * slotId;
                            totalMultiplier += multiplier * slotId;
                            winningSlots.addAll(combination);
                        }
                    }

                }
            }
        }

        Map<Integer, Slot> transformedMap = slotsMatrix.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new Slot(entry.getValue(), winningSlots.contains(entry.getKey()))
                ));

        transformedMap.entrySet().forEach(System.out::println);

        SlotsGameEntity slotsGameEntity = SlotsGameEntity.builder()
                .slotMatrix(transformedMap)
                .date(new Date())
                .userId(userId)
                .isDone(false)
                .stake(stake)
                .winAmount(winAmount)
                .multiplier(totalMultiplier)
                .build();

        System.out.println(slotsGameEntity);

        return slotsGameEntity;
    }


    @Transactional
    public SlotsGameEntity startSlotGame(long stake, long userId){
        SlotsGameEntity spin = this.spin(stake,userId);
        return slotsGameRepository.save(spin);
    }

    public void makeSlotGameDone(Long slotGameId){
        System.out.println(slotGameId);
        Optional<SlotsGameEntity> byId = slotsGameRepository.findById(slotGameId);
        if(byId.isPresent()){
            byId.get().setDone(true);
            slotsGameRepository.saveAndFlush(byId.get());
        }else {

            throw new SlotGameNotFoundException("slots game not found");
        }
    }

    public List<SlotsGameEntityDTO> getWinningSlotsGames(long userId){
        List<SlotsGameEntity> winningSlots = slotsGameRepository.findAllByUserIdAndWinAmountGreaterThanAndIsDoneIsTrue(userId, 0);
        if(winningSlots.isEmpty()){
            return null;
        }

        return winningSlots.stream().map(elem-> new SlotsGameEntityDTO(elem.getStake(),elem.getDate(),elem.isDone(),elem.getWinAmount(),elem.getMultiplier())).sorted(new Comparator<SlotsGameEntityDTO>() {
            @Override
            public int compare(SlotsGameEntityDTO o1, SlotsGameEntityDTO o2) {
                return Long.compare(o1.getWinAmount(), o2.getWinAmount());
            }
        }).toList();

    }
}
