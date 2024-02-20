package edu.yu.da;

public class PickAYeshiva extends PickAYeshivaBase {


    /**
     * Constructor which supplies the yeshiva rankings in terms of two factors
     * of interest.  The constructor executes a divide-and-conquer algorithm to
     * determine the minimum number of yeshiva-to-yeshiva comparisons required to
     * make a "which yeshiva to attend" decision.  The getters can be accessed in
     * O(1) time after the constructor executes successfully.
     * <p>
     * It is the client's responsibility to ensure that no pair of
     * facultyRatioRankings and cookingRankings values are duplicates.
     *
     * @param facultyRatioRankings Array whose ith element is the value of the
     *                             ith yeshiva with respect to its faculty-to-student ratio (Rabbeim etc).
     *                             Client maintains ownership.  Can't be null and must be same length as the
     *                             other parameter.
     * @param cookingRankings      Array whose ith element is the value of the ith
     *                             yeshiva with respect to the quality of the cooking.  Client maintains
     *                             ownership.  Can't be null and must be same length as other parameter.
     * @throws IllegalArgumentException if pre-conditions are violated.
     */
    public PickAYeshiva(double[] facultyRatioRankings, double[] cookingRankings) {
        super(facultyRatioRankings, cookingRankings);
    }

    @Override
    public double[] getFacultyRatioRankings() {
        return new double[0];
    }

    @Override
    public double[] getCookingRankings() {
        return new double[0];
    }
}
