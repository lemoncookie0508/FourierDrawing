package lepl.fouriertransform;

/**
 * 이산 푸리에 변환의 주파수 집합을 나타내는 인터페이스
 */
public interface FrequencyFunction {
    /**
     * @param index 주파수 인덱수
     * @return {@code index}번째 주파수
     */
    int get(int index);
}
